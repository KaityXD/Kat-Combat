package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.models.Session
import com.kaity.katcombat.utils.Messages.sendActionBarMini
import com.kaity.katcombat.utils.Messages.sendMessageMini
import com.kaity.katcombat.utils.Placeholders
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import java.util.concurrent.ConcurrentHashMap

class Combat(private val plugin: KatCombat, val config: Config) {
    private val sessions = ConcurrentHashMap<Player, Session>()

    fun isInCombat(player: Player): Boolean {
        val session = sessions[player] ?: return false
        return System.currentTimeMillis() < session.endTime
    }

    fun getRemainingTime(player: Player): Long {
        return sessions[player]?.getRemainingTime() ?: 0
    }

    fun tagPlayer(player: Player, killer: Player? = null) {
        val duration = config.combatDuration * 1000L
        val combatEnd = System.currentTimeMillis() + duration
        
        val isNewCombat = !isInCombat(player)
        
        val session = sessions.getOrPut(player) {
            Session(
                endTime = combatEnd,
                killer = killer?.name,
                originalFlightAllowed = player.allowFlight,
                originalFlying = player.isFlying
            )
        }
        
        session.endTime = combatEnd
        killer?.let { session.killer = it.name }
        
        if (isNewCombat) {
            handleEnterCombat(player)
        }
        
        startActionBarTask(player, session)
    }

    private fun handleEnterCombat(player: Player) {
        val message = Placeholders.apply(config.getMessage("combat-tagged"), player)
        player.sendMessageMini(message)
        
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

    private fun startActionBarTask(player: Player, session: Session) {
        session.actionBarTask?.cancel()
        
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

    fun untagPlayer(player: Player) {
        val session = sessions.remove(player)
        session?.actionBarTask?.cancel()
        
        if (session != null) {
            val expiredMsg = config.getMessage("combat-expired")
            player.sendMessageMini(expiredMsg)
        }
    }

    fun getKiller(player: Player): String? {
        return sessions[player]?.killer
    }
    
    fun getSession(player: Player): Session? {
        return sessions[player]
    }

    fun broadcastCombatLogger(player: Player) {
        val message = config.getMessage("combat-logger-death").replace("<player>", player.name)
        
        if (config.broadcastDeaths) {
            plugin.server.broadcast(com.kaity.katcombat.utils.Messages.parse(message))
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

    fun cleanup() {
        sessions.values.forEach { it.actionBarTask?.cancel() }
        sessions.clear()
    }

    fun getDeathMessage(player: Player, cause: DamageCause): String {
        val messageKey = when (cause) {
            DamageCause.FALL -> "death-fall"
            DamageCause.LAVA -> "death-lava"
            DamageCause.FIRE -> "death-fire"
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
            else -> "death-default"
        }

        val message = config.getMessage(messageKey)
        return message.replace("<player>", player.name)
    }
}
