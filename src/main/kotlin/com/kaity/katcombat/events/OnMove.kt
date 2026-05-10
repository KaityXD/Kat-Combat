package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class OnMove(private val combat: Combat) : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player
        if (player.isDead) return
        if (!player.isGliding) return
        
        val maxDistance = combat.config.killOnElytraDistance
        if (maxDistance <= 0) return
        
        if (!combat.isInCombat(player)) return
        val session = combat.getSession(player) ?: return
        
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
