package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import com.kaity.katcombat.managers.EffectManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnKilled(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onKilled(e: PlayerDeathEvent) {
        val victim = e.entity
        val killer = victim.killer

        if (killer != null && killer is org.bukkit.entity.Player) {
            if (combatManager.isInCombat(victim) || combatManager.isInCombat(killer)) {
                combatManager.broadcastPlayerKilled(killer, victim)

                if (EffectManager.hasEffectEnabled(killer)) {
                    EffectManager.playDeathEffects(killer, victim)
                }
            }
            combatManager.untagPlayer(victim)
        }
    }
}
