package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.utils.Messages
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnDied(private val combat: Combat) : Listener {

    @EventHandler
    fun onDied(e: PlayerDeathEvent) {
        val victim = e.entity
        val killer = victim.killer

        if (killer == null) {
            val cause = victim.lastDamageCause?.cause
            if (cause != null && combat.isInCombat(victim)) {
                val deathMessage = combat.getDeathMessage(victim, cause)
                if (deathMessage.isNotEmpty()) {
                    if (combat.config.broadcastDeaths) {
                        e.deathMessage(Messages.parse(deathMessage))
                    } else {
                        e.deathMessage(null)
                    }
                }

                combat.playDeathSound(victim)
            }
            combat.untagPlayer(victim)
        }
    }
}
