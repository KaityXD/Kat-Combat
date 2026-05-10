package com.kaity.katcombat.events

import com.kaity.katcombat.managers.CombatManager
import com.kaity.katcombat.utils.MessageUtils.sendMessageMini
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class OnCommand(private val combatManager: CombatManager) : Listener {

    @EventHandler
    fun onCommand(e: PlayerCommandPreprocessEvent) {
        if (!combatManager.isInCombat(e.player)) return
        
        val command = e.message.removePrefix("/").lowercase().split(" ")[0]
        
        if (combatManager.config.isCommandBlocked(command)) {
            val msg = combatManager.config.getMessage("command-blocked").replace("<cmd>", command)
            e.player.sendMessageMini(msg)
            e.isCancelled = true
        }
    }
}
