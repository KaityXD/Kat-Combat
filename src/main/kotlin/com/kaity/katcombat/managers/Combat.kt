package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.models.Session
import com.kaity.katcombat.utils.Messages.parse
import com.kaity.katcombat.utils.Messages.sendActionBarMini
import com.kaity.katcombat.utils.Messages.sendMessageMini
import com.kaity.katcombat.utils.Placeholders
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class Combat(private val plugin: KatCombat, val config: Config) {
    private val sessions = ConcurrentHashMap<UUID, Session>()
    private val combatLoggers = ConcurrentHashMap<UUID, Long>()

    fun isInCombat(player: Player): Boolean {
        val session = sessions[player.uniqueId] ?: return false
        return System.currentTimeMillis() < session.endTime
    }

    fun getRemainingTime(player: Player): Long {
        return sessions[player.uniqueId]?.getRemainingTime() ?: 0
    }

    fun tagPlayer(player: Player, killer: Player? = null) {
        val duration = config.combatDuration * 1000L
        val combatEnd = System.currentTimeMillis() + duration

        val isNewCombat = !isInCombat(player)

        val session = sessions.getOrPut(player.uniqueId) {
            Session(
                endTime = combatEnd,
                killer = killer?.name,
                originalFlightAllowed = player.allowFlight,
                originalFlying = player.isFlying,
                originalGameMode = player.gameMode
            )
        }

        session.endTime = combatEnd
        killer?.let { session.killer = it.name }

        if (isNewCombat) {
            handleEnterCombat(player)
        }

        startTimerTask(player, session)
    }

    private fun handleEnterCombat(player: Player) {
        val message = Placeholders.apply(config.getMessage("combat-tagged"), player)
        if (message.isNotEmpty()) {
            player.sendMessageMini(message)
        }

        if (config.forceSurvival && player.gameMode != GameMode.SURVIVAL) {
            player.gameMode = GameMode.SURVIVAL
        }

        if (config.disableFlight) {
            if (player.isFlying) {
                player.isFlying = false
            }
            if (player.allowFlight) {
                player.allowFlight = false
            }
        }
    }

    private fun startTimerTask(player: Player, session: Session) {
        session.actionBarTask?.cancel()
        session.bossBarTask?.cancel()
        session.bossBar?.removeAll()
        session.bossBar?.isVisible = false

        if (config.useBossBar) {
            startBossBarTask(player, session)
        } else if (config.showActionbarTimer) {
            startActionBarTask(player, session)
        }
    }

    private fun startActionBarTask(player: Player, session: Session) {
        if (!config.showActionbarTimer) return

        val task = player.scheduler.runAtFixedRate(plugin, { _ ->
            if (!isInCombat(player)) {
                untagPlayer(player)
                return@runAtFixedRate
            }

            val remaining = getRemainingTime(player)
            var msg = config.getMessage("combat-actionbar").replace("<time>", remaining.toString())
            msg = Placeholders.apply(msg, player)
            player.sendActionBarMini(msg)
        }, null, 1L, 20L)

        if (task != null) {
            session.actionBarTask = task
        }
    }

    private fun startBossBarTask(player: Player, session: Session) {
        val bar = plugin.server.createBossBar("Combat", BarColor.RED, BarStyle.SOLID)
        bar.addPlayer(player)
        bar.isVisible = true
        session.bossBar = bar

        val total = config.combatDuration.toDouble()

        val task = player.scheduler.runAtFixedRate(plugin, { _ ->
            if (!isInCombat(player)) {
                bar.removeAll()
                bar.isVisible = false
                untagPlayer(player)
                return@runAtFixedRate
            }

            val remaining = getRemainingTime(player)
            bar.progress = (remaining / total).coerceIn(0.0, 1.0)
            bar.setTitle("Combat: ${remaining}s")
        }, null, 1L, 20L)

        if (task != null) {
            session.bossBarTask = task
        }
    }

    fun untagPlayer(player: Player) {
        val session = sessions.remove(player.uniqueId)
        session?.actionBarTask?.cancel()
        session?.bossBarTask?.cancel()
        session?.bossBar?.removeAll()
        session?.bossBar?.isVisible = false

        if (session != null && player.isOnline) {
            if (config.disableFlight) {
                player.allowFlight = session.originalFlightAllowed
                if (session.originalFlying && session.originalFlightAllowed) {
                    player.isFlying = true
                }
            }

            if (config.forceSurvival) {
                player.gameMode = session.originalGameMode
            }

            val expiredMsg = config.getMessage("combat-expired")
            if (expiredMsg.isNotEmpty()) {
                player.sendMessageMini(expiredMsg)
            }
        }
    }

    fun getKiller(player: Player): String? {
        return sessions[player.uniqueId]?.killer
    }

    fun getSession(player: Player): Session? {
        return sessions[player.uniqueId]
    }

    fun getCombatPlayers(): List<Player> {
        return sessions.keys.mapNotNull { plugin.server.getPlayer(it) }.filter { isInCombat(it) }
    }

    fun broadcastCombatLogger(player: Player) {
        val message = config.getMessage("combat-logger-death").replace("<player>", player.name)

        if (config.broadcastDeaths) {
            plugin.server.broadcast(parse(message))
        }

        if (config.logToConsole) {
            plugin.logger.info("[CombatLog] ${player.name} disconnected while in combat!")
        }
    }

    fun handlePlayerKilled(killer: Player, victim: Player, event: org.bukkit.event.entity.PlayerDeathEvent, message: Component) {
        if (config.broadcastDeaths) {
            event.deathMessage(message)
        } else {
            event.deathMessage(null)
            killer.sendMessage(message)
            victim.sendMessage(message)
        }

        if (config.logToConsole) {
            plugin.logger.info("[CombatLog] ${killer.name} killed ${victim.name} while in combat!")
        }
    }

    fun playDeathSound(player: Player) {
        val soundName = config.deathSound
        if (soundName.uppercase() == "NONE") return
        try {
            val key = NamespacedKey.minecraft(soundName.lowercase())
            val sound = Registry.SOUNDS.get(key)
            if (sound != null) {
                player.world.playSound(player.location, sound, 1.0f, 1.0f)
            }
        } catch (_: Exception) {
            // Invalid sound in config, ignore
        }
    }

    fun cleanup() {
        sessions.values.forEach {
            it.actionBarTask?.cancel()
            it.bossBarTask?.cancel()
            it.bossBar?.removeAll()
            it.bossBar?.isVisible = false
        }
        sessions.clear()
        combatLoggers.clear()
    }

    fun getDeathMessage(player: Player, cause: DamageCause): String {
        val messageKey = when (cause) {
            DamageCause.FALL -> "death-fall"
            DamageCause.LAVA -> "death-lava"
            DamageCause.FIRE, DamageCause.FIRE_TICK -> "death-fire"
            DamageCause.DROWNING -> "death-drowning"
            DamageCause.VOID -> "death-void"
            DamageCause.ENTITY_ATTACK -> "death-mob"
            DamageCause.ENTITY_EXPLOSION -> "death-explosion"
            DamageCause.MAGIC -> "death-magic"
            DamageCause.POISON -> "death-poison"
            DamageCause.WITHER -> "death-wither"
            DamageCause.CONTACT -> "death-cactus"
            DamageCause.FALLING_BLOCK -> "death-falling-block"
            DamageCause.LIGHTNING -> "death-lightning"
            DamageCause.STARVATION -> "death-starvation"
            DamageCause.SUFFOCATION -> "death-suffocation"
            DamageCause.THORNS -> "death-thorns"
            DamageCause.HOT_FLOOR -> "death-hot-floor"
            DamageCause.CRAMMING -> "death-cramming"
            DamageCause.FREEZE -> "death-freeze"
            DamageCause.DRAGON_BREATH -> "death-dragon-breath"
            DamageCause.FLY_INTO_WALL -> "death-fly-into-wall"
            DamageCause.SONIC_BOOM -> "death-sonic-boom"
            else -> "death-default"
        }

        val message = config.getMessage(messageKey)
        return message.replace("<player>", player.name)
    }

    // Combat logger tracking (for rejoin punishment)
    fun markCombatLog(player: Player) {
        combatLoggers[player.uniqueId] = System.currentTimeMillis()
    }

    fun hasCombatLog(player: Player): Boolean {
        val time = combatLoggers[player.uniqueId] ?: return false
        return System.currentTimeMillis() < time + config.combatDuration * 1000L
    }

    fun clearCombatLog(player: Player) {
        combatLoggers.remove(player.uniqueId)
    }
}
