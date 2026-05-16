package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.WorldGuardHelper
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PotionSplashEvent

class OnAttacked(private val combat: Combat) : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAttacked(e: EntityDamageByEntityEvent) {
        val damaged = e.entity as? org.bukkit.entity.Player ?: return
        var damager = e.damager

        if (damager is org.bukkit.entity.Projectile) {
            val shooter = damager.shooter
            if (shooter is org.bukkit.entity.Player) {
                damager = shooter
            }
        }

        val damagerPlayer = damager as? org.bukkit.entity.Player ?: return

        if (damaged == damagerPlayer) return

        if (!WorldGuardHelper.isPvpAllowed(damaged.location) || !WorldGuardHelper.isPvpAllowed(damagerPlayer.location)) {
            return
        }

        combat.tagPlayer(damaged, damagerPlayer)
        combat.tagPlayer(damagerPlayer, damaged)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPotionSplash(e: PotionSplashEvent) {
        val thrower = e.entity.shooter as? org.bukkit.entity.Player ?: return
        for (entity in e.affectedEntities) {
            if (entity is org.bukkit.entity.Player && entity != thrower) {
                if (!WorldGuardHelper.isPvpAllowed(entity.location) || !WorldGuardHelper.isPvpAllowed(thrower.location)) {
                    continue
                }
                combat.tagPlayer(entity, thrower)
                combat.tagPlayer(thrower, entity)
            }
        }
    }
}
