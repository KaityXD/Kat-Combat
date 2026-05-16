package com.kaity.katcombat.utils

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CommandBuilder(private val name: String, private val description: String = "") {
    private var permission: String? = null
    private var executionPlayer: ((Player, Array<out String>) -> Unit)? = null
    private var executionSender: ((CommandSender, Array<out String>) -> Unit)? = null
    private var suggestion: ((CommandSender, Array<out String>) -> List<String>)? = null

    fun permission(perm: String) = apply { this.permission = perm }
    fun execute(block: (Player, Array<out String>) -> Unit) = apply { this.executionPlayer = block }
    fun executeSender(block: (CommandSender, Array<out String>) -> Unit) = apply { this.executionSender = block }
    fun suggest(block: (CommandSender, Array<out String>) -> List<String>) = apply { this.suggestion = block }

    fun register(plugin: JavaPlugin) {
        val basicCommand = object : BasicCommand {
            override fun execute(stack: CommandSourceStack, args: Array<out String>) {
                val sender = stack.sender
                if (sender is Player) {
                    executionPlayer?.invoke(sender, args)
                }
                executionSender?.invoke(sender, args)
            }

            override fun suggest(stack: CommandSourceStack, args: Array<out String>): Collection<String> {
                val sender = stack.sender
                return suggestion?.invoke(sender, args) ?: emptyList()
            }

            override fun permission(): String? = permission
        }

        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(name, description, basicCommand)
        }
    }
}
