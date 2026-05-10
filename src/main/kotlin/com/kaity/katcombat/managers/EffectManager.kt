package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

object EffectManager {
    private val effectKey = NamespacedKey(KatCombat.instance, "killeffect")

    fun toggleEffect(player: Player): Boolean {
        val container = player.persistentDataContainer
        val currentState = container.getOrDefault(effectKey, PersistentDataType.BYTE, 0.toByte())
        
        val newState: Byte = if (currentState == 1.toByte()) 0 else 1
        container.set(effectKey, PersistentDataType.BYTE, newState)
        
        return newState == 1.toByte()
    }

    fun hasEffectEnabled(player: Player): Boolean {
        return player.persistentDataContainer.getOrDefault(effectKey, PersistentDataType.BYTE, 0.toByte()) == 1.toByte()
    }

    fun playDeathEffects(killer: Player, victim: Player) {
        // Killer effect (ominous circle)
        playCircleEffect(killer.location, "TRIAL_SPAWNER_DETECTION_OMINOUS")

        // Victim effect (normal circle)
        val victimLoc = victim.location
        playCircleEffect(victimLoc, "TRIAL_SPAWNER_DETECTION")

        // Soul particles and sounds at victim location (original logic)
        val world = victimLoc.world ?: return
        val center = victimLoc.clone().add(0.0, 1.0, 0.0)

        world.playSound(center, Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 1.0f)
        world.playSound(center, Sound.BLOCK_SOUL_SAND_STEP, 1.0f, 0.5f)

        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 25, 0.5, 0.5, 0.5, 0.05)
        world.spawnParticle(Particle.SOUL, center, 20, 0.4, 0.5, 0.4, 0.1)
    }

    private fun playCircleEffect(location: Location, particleName: String) {
        val world = location.world ?: return
        val particle = try {
            Particle.valueOf(particleName)
        } catch (e: Exception) {
            return
        }

        val radius = 0.8
        val points = 20
        val yOffset = 0.5

        for (i in 0 until points) {
            val angle = 2.0 * Math.PI * i / points
            val x = radius * Math.cos(angle)
            val z = radius * Math.sin(angle)
            val particleLoc = location.clone().add(x, yOffset, z)
            world.spawnParticle(particle, particleLoc, 1, 0.0, 0.0, 0.0, 0.0)
        }
    }

    @Deprecated("Use playDeathEffects(killer, victim) instead", ReplaceWith("playDeathEffects(killer, victim)"))
    fun playKillEffect(location: Location) {
        val world = location.world ?: return
        val center = location.clone().add(0.0, 1.0, 0.0)

        // Soul sand / soul speed sound
        world.playSound(center, Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 1.0f)
        world.playSound(center, Sound.BLOCK_SOUL_SAND_STEP, 1.0f, 0.5f)

        // Blue soul fire flames
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, center, 25, 0.5, 0.5, 0.5, 0.05)
        
        // Soul particles rising up
        world.spawnParticle(Particle.SOUL, center, 20, 0.4, 0.5, 0.4, 0.1)
    }
}