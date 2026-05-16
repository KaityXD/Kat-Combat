package com.kaity.katcombat.events

import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.utils.Messages.sendMessageMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class OnCommand(private val combat: Combat) : Listener {

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (!combat.isInCombat(e.player)) return

        val command = e.message.removePrefix("/").lowercase().split(" ")[0]

        if (combat.config.isCommandBlocked(command)) {
            val msg = combat.config.getMessage("command-blocked").replace("<cmd>", command)
            e.player.sendMessageMini(msg)
            e.isCancelled = true
        }
    }
}
