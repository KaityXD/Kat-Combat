package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.WorldGuardHelper
import com.kaity.katcombat.utils.Messages.sendMessageMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class OnMove(private val combat: Combat) : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player
        if (player.isDead) return

        // Elytra distance check
        if (player.isGliding) {
            val maxDistance = combat.config.killOnElytraDistance
            if (maxDistance > 0) {
                if (combat.isInCombat(player)) {
                    val session = combat.getSession(player) ?: return
                    val startLoc = session.elytraStartLocation ?: return

                    if (startLoc.world != player.world) {
                        session.elytraStartLocation = player.location
                        return
                    }

                    val distance = startLoc.distance(player.location)
                    if (distance >= maxDistance) {
                        player.health = 0.0
                        session.elytraStartLocation = null
                    }
                }
            }
        }

        // Safe zone entry block
        if (!combat.config.blockSafezoneEntry) return
        if (!combat.isInCombat(player)) return
        if (!WorldGuardHelper.isEnabled()) return

        val from = e.from
        val to = e.to ?: return

        val fromPvp = WorldGuardHelper.isPvpAllowed(from)
        val toPvp = WorldGuardHelper.isPvpAllowed(to)

        if (fromPvp && !toPvp) {
            e.isCancelled = true
            val message = combat.config.getMessage("safezone-blocked")
            if (message.isNotBlank()) {
                player.sendMessageMini(message)
            }
        }
    }
}
