package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class OnQuit(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        
        if (combatManager.isInCombat(player)) {
            combatManager.broadcastCombatLogger(player)
            player.health = 0.0 // Kill for combat logging
        }
        
        combatManager.untagPlayer(player)
    }
}
