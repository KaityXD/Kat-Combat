package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class OnMove(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player
        if (!player.isGliding) return
        
        val maxDistance = combatManager.config.killOnElytraDistance
        if (maxDistance <= 0) return
        
        if (!combatManager.isInCombat(player)) return
        val session = combatManager.getSession(player) ?: return
        
        val startLoc = session.elytraStartLocation ?: return
        
        if (startLoc.world != player.world) {
            session.elytraStartLocation = player.location
            return
        }
        
        val distance = startLoc.distance(player.location)
        if (distance >= maxDistance) {
            // Kill player
            player.health = 0.0
            session.elytraStartLocation = null // Reset
        }
    }
}
