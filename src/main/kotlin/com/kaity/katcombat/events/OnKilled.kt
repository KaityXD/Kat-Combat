package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Effects
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class OnKilled(private val combat: Combat) : Listener {

    @EventHandler
    fun onKilled(e: PlayerDeathEvent) {
        val victim = e.entity
        val killer = victim.killer

        if (killer != null) {
            if (combat.isInCombat(victim) || combat.isInCombat(killer)) {
                combat.handlePlayerKilled(killer, victim, e)
                
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

                if (Effects.hasEffectEnabled(killer)) {
                    Effects.playDeathEffects(killer, victim)
                }
            }
            combat.untagPlayer(victim)
        }
    }
}
