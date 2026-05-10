package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent

class OnToggleGlide(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onToggleGlide(e: EntityToggleGlideEvent) {
        val player = e.entity as? Player ?: return
        if (!combatManager.isInCombat(player)) return
        
        val session = combatManager.getSession(player) ?: return
        
        if (e.isGliding) {
            // Started gliding
            session.elytraStartLocation = player.location
        } else {
            // Stopped gliding
            session.elytraStartLocation = null
        }
    }
}
