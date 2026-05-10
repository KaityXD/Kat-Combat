package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class OnAttacked(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onAttacked(e: EntityDamageByEntityEvent) {
        val damaged = e.entity as? org.bukkit.entity.Player ?: return
        val damager = e.damager as? org.bukkit.entity.Player ?: return

        if (damaged == damager) return

        combatManager.tagPlayer(damaged, damager)
        combatManager.tagPlayer(damager, damaged)
    }
}
