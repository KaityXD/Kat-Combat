package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class OnDropItem(private val combat: Combat) : Listener {

    @EventHandler
    fun onDrop(e: PlayerDropItemEvent) {
        if (!combat.isInCombat(e.player)) return
        if (!combat.config.blockItemDrop) return

        e.isCancelled = true
    }
}
