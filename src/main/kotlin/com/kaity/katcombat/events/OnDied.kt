package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.utils.Messages
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
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
                
                // Play death sound
                val soundName = combat.config.deathSound
                if (soundName.uppercase() != "NONE") {
                    try {
                        val key = NamespacedKey.minecraft(soundName.lowercase())
                        val sound = Registry.SOUNDS.get(key)
                        if (sound != null) {
                            victim.world.playSound(victim.location, sound, 1.0f, 1.0f)
                        }
                    } catch (ex: Exception) {
                        // Invalid sound in config, ignore
                    }
                }
            }
            combat.untagPlayer(victim)
        }
    }
}
