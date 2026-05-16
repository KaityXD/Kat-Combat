package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class OnJoin(private val combat: Combat) : Listener {

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        if (combat.hasCombatLog(player)) {
            if (!player.isDead) {
                player.health = 0.0
            }
            combat.clearCombatLog(player)
        }
    }
}
