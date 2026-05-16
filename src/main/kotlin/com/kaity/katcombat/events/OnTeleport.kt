package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class OnTeleport(private val combat: Combat) : Listener {

    @EventHandler
    fun onTeleport(e: PlayerTeleportEvent) {
        if (!combat.isInCombat(e.player)) return
        if (!combat.config.blockTeleport) return

        // Allow natural spectator teleport
        if (e.cause == PlayerTeleportEvent.TeleportCause.SPECTATE) return

        e.isCancelled = true
    }
}
