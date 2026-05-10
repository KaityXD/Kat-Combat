package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.models.CombatSession
import com.kaity.katcombat.utils.MessageUtils.sendActionBarMini
import com.kaity.katcombat.utils.MessageUtils.sendMessageMini
import com.kaity.katcombat.utils.PlaceholderUtils
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import java.util.concurrent.ConcurrentHashMap

class CombatManager(private val plugin: KatCombat, val config: ConfigManager) {
    private val sessions = ConcurrentHashMap<Player, CombatSession>()

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
            CombatSession(
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
        val message = PlaceholderUtils.apply(config.getMessage("combat-tagged"), player)
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

    private fun startActionBarTask(player: Player, session: CombatSession) {
        session.actionBarTask?.cancel()
        
        if (!config.showActionbarTimer) return
        
        val task = player.scheduler.runAtFixedRate(plugin, { _ ->
            if (!isInCombat(player)) {
                untagPlayer(player)
                return@runAtFixedRate
            }
            
            val remaining = getRemainingTime(player)
            var msg = config.getMessage("combat-actionbar").replace("<time>", remaining.toString())
            msg = PlaceholderUtils.apply(msg, player)
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
            // Revert flight state if necessary? 
            // Often plugins don't revert automatically to avoid abuse, but let's do it if they had it.
            // Actually, maybe not restoring it to avoid giving free flight in survival. 
            // Just sending expired message.
            val expiredMsg = config.getMessage("combat-expired")
            player.sendMessageMini(expiredMsg)
        }
    }

    fun getKiller(player: Player): String? {
        return sessions[player]?.killer
    }
    
    fun getSession(player: Player): CombatSession? {
        return sessions[player]
    }

    fun broadcastCombatLogger(player: Player) {
        val message = config.getMessage("combat-logger-death").replace("<player>", player.name)
        
        if (config.broadcastDeaths) {
            plugin.server.broadcast(com.kaity.katcombat.utils.MessageUtils.parse(message))
        }
        
        if (config.logToConsole) {
            plugin.logger.info("[CombatLog] ${player.name} disconnected while in combat!")
        }
    }

    fun broadcastPlayerKilled(killer: Player, victim: Player) {
        val message = config.getMessage("player-killed")
            .replace("<killer>", killer.name)
            .replace("<player>", victim.name)
        
        if (config.broadcastDeaths) {
            plugin.server.broadcast(com.kaity.katcombat.utils.MessageUtils.parse(message))
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