package com.kaity.katcombat.commands

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Effects
import com.kaity.katcombat.utils.CommandBuilder
import com.kaity.katcombat.utils.Messages.sendMessageMini

class Command(private val plugin: KatCombat, private val combat: Combat) {

    fun register() {
        CommandBuilder("combat", "Combat commands")
            .execute { player, args ->
                if (args.isEmpty()) {
                    if (combat.isInCombat(player)) {
                        val remaining = combat.getRemainingTime(player)
                        val killer = combat.getKiller(player)
                        
                        val message = if (killer != null) {
                            "<red>Combat: <gold>${remaining}s <dark_gray>| Killer: <red>$killer"
                        } else {
                            "<red>Combat: <gold>${remaining}s"
                        }
                        player.sendMessageMini(message)
                    } else {
                        player.sendMessageMini("<green>You are not in combat.")
                    }
                    return@execute
                }

                when (args[0].lowercase()) {
                    "reload" -> {
                        if (!player.hasPermission("katcombat.admin")) {
                            player.sendMessageMini("<red>You don't have permission!")
                            return@execute
                        }
                        combat.config.reload()
                        player.sendMessageMini("<green>Config reloaded!")
                    }
                    "effect" -> {
                        val enabled = Effects.toggleEffect(player)
                        if (enabled) {
                            player.sendMessageMini("<green>Kill effect <bold>ENABLED</bold>!")
                        } else {
                            player.sendMessageMini("<red>Kill effect <bold>DISABLED</bold>!")
                        }
                    }
                    else -> {
                        player.sendMessageMini("<red>Usage: /combat [reload|effect]")
                    }
                }
            }
            .suggest { player, args ->
                val options = mutableListOf("effect")
                if (player.hasPermission("katcombat.admin")) {
                    options.add("reload")
                }
                
                when (args.size) {
                    1 -> options.filter { it.startsWith(args[0], true) }
                    else -> emptyList()
                }
            }
            .register(plugin)

        CommandBuilder("kcview", "View a player's death inventory")
            .execute { player, args ->
                if (args.isEmpty()) {
                    player.sendMessageMini("<red>Usage: /kcview <key>")
                    return@execute
                }
                plugin.inventoryViewer.openInventory(player, args[0])
            }
            .register(plugin)
    }
}
