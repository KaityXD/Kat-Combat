package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class OnQuit(private val combat: Combat) : Listener {

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player

        if (combat.isInCombat(player)) {
            if (!player.isDead) {
                if (combat.config.preventDisconnect) {
                    combat.broadcastCombatLogger(player)
                    player.health = 0.0
                } else {
                    combat.markCombatLog(player)
                }
            }
        }

        combat.untagPlayer(player)
    }
}
