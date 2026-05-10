package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnDied(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onDied(e: PlayerDeathEvent) {
        val victim = e.entity
        val killer = victim.killer

        if (killer == null || killer !is org.bukkit.entity.Player) {
            val cause = victim.lastDamageCause?.cause
            if (cause != null && combatManager.isInCombat(victim)) {
                val deathMessage = combatManager.getDeathMessage(victim, cause)
                if (deathMessage.isNotEmpty()) {
                    e.deathMessage(com.kaity.katcombat.utils.MessageUtils.parse(deathMessage))
                }
            }
            combatManager.untagPlayer(victim)
        }
    }
}
