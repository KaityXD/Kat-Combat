package com.kaity.katcombat.commands

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Effects
import com.kaity.katcombat.utils.CommandBuilder
import com.kaity.katcombat.utils.Messages.parse
import com.kaity.katcombat.utils.Messages.sendMessageMini
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command(private val plugin: KatCombat, private val combat: Combat) {

    fun register() {
        CommandBuilder("combat", "Combat commands")
            .executeSender { sender, args ->
                if (sender !is Player) {
                    if (args.isNotEmpty() && args[0].lowercase() == "reload") {
                        if (!sender.hasPermission("katcombat.admin")) {
                            sender.sendMessage(parse("<red>Console requires katcombat.admin permission."))
                            return@executeSender
                        }
                        combat.config.reload()
                        sender.sendMessage(parse("<green>KatCombat config reloaded!"))
                    } else {
                        sender.sendMessage(parse("<red>Console usage: /combat reload"))
                    }
                }
            }
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
                    "tag" -> {
                        if (!player.hasPermission("katcombat.admin")) {
                            player.sendMessageMini("<red>You don't have permission!")
                            return@execute
                        }
                        val target = if (args.size > 1) plugin.server.getPlayer(args[1]) else null
                        if (target == null) {
                            player.sendMessageMini("<red>Usage: /combat tag <player>")
                            return@execute
                        }
                        combat.tagPlayer(target, player)
                        player.sendMessageMini("<green>Tagged ${target.name} for ${combat.config.combatDuration}s!")
                    }
                    "untag" -> {
                        if (!player.hasPermission("katcombat.admin")) {
                            player.sendMessageMini("<red>You don't have permission!")
                            return@execute
                        }
                        val target = if (args.size > 1) plugin.server.getPlayer(args[1]) else null
                        if (target == null) {
                            player.sendMessageMini("<red>Usage: /combat untag <player>")
                            return@execute
                        }
                        combat.untagPlayer(target)
                        player.sendMessageMini("<green>Removed ${target.name} from combat!")
                    }
                    "status" -> {
                        if (!player.hasPermission("katcombat.admin")) {
                            player.sendMessageMini("<red>You don't have permission!")
                            return@execute
                        }
                        val target = if (args.size > 1) plugin.server.getPlayer(args[1]) else null
                        if (target == null) {
                            player.sendMessageMini("<red>Usage: /combat status <player>")
                            return@execute
                        }
                        val remaining = combat.getRemainingTime(target)
                        val inCombat = combat.isInCombat(target)
                        val killer = combat.getKiller(target)
                        if (inCombat) {
                            player.sendMessageMini("<yellow>${target.name} is in combat for ${remaining}s. Killer: ${killer ?: "None"}")
                        } else {
                            player.sendMessageMini("<green>${target.name} is not in combat.")
                        }
                    }
                    "list" -> {
                        if (!player.hasPermission("katcombat.admin")) {
                            player.sendMessageMini("<red>You don't have permission!")
                            return@execute
                        }
                        val list = combat.getCombatPlayers()
                        if (list.isEmpty()) {
                            player.sendMessageMini("<gray>No players in combat.")
                        } else {
                            player.sendMessageMini("<yellow>Players in combat:")
                            list.forEach {
                                player.sendMessageMini("<gray>- ${it.name} (${combat.getRemainingTime(it)}s)")
                            }
                        }
                    }
                    "help" -> sendHelp(player)
                    "version" -> player.sendMessageMini("<yellow>KatCombat v${plugin.description.version}")
                    else -> sendHelp(player)
                }
            }
            .suggest { sender, args ->
                val isPlayer = sender is Player
                val options = mutableListOf("effect", "help", "version")
                if (sender.hasPermission("katcombat.admin")) {
                    options.addAll(listOf("reload", "tag", "untag", "status", "list"))
                }

                when (args.size) {
                    1 -> options.filter { it.startsWith(args[0], true) }
                    2 -> when (args[0].lowercase()) {
                        "tag", "untag", "status" -> plugin.server.onlinePlayers
                            .filter { it != sender }
                            .map { it.name }
                            .filter { it.startsWith(args[1], true) }
                        else -> emptyList()
                    }
                    else -> emptyList()
                }
            }
            .register(plugin)

        CommandBuilder("kcview", "View a player's death inventory")
            .permission("katcombat.viewinventory")
            .executeSender { sender, args ->
                if (sender is Player) {
                    if (args.isEmpty()) {
                        sender.sendMessageMini("<red>Usage: /kcview <key>")
                        return@executeSender
                    }
                    plugin.inventoryViewer.openInventory(sender, args[0])
                } else {
                    sender.sendMessage(parse("<red>Only players can use /kcview"))
                }
            }
            .register(plugin)
    }

    private fun sendHelp(player: Player) {
        player.sendMessageMini("<yellow>KatCombat Commands:")
        player.sendMessageMini("<gray>/combat - Check your combat status")
        player.sendMessageMini("<gray>/combat effect - Toggle kill effects")
        player.sendMessageMini("<gray>/combat help - Show this help")
        player.sendMessageMini("<gray>/combat version - Show plugin version")
        if (player.hasPermission("katcombat.admin")) {
            player.sendMessageMini("<gray>/combat reload - Reload config")
            player.sendMessageMini("<gray>/combat tag <player> - Tag a player")
            player.sendMessageMini("<gray>/combat untag <player> - Untag a player")
            player.sendMessageMini("<gray>/combat status <player> - Check player status")
            player.sendMessageMini("<gray>/combat list - List players in combat")
        }
    }
}
