package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerFishEvent

class OnFish(private val combat: Combat) : Listener {

    @EventHandler
    fun onFish(e: PlayerFishEvent) {
        if (e.state != PlayerFishEvent.State.CAUGHT_ENTITY) return
        val caught = e.caught as? Player ?: return
        val fisher = e.player
        if (caught == fisher) return

        combat.tagPlayer(caught, fisher)
        combat.tagPlayer(fisher, caught)
    }
}
