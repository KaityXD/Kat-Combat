package com.kaity.katcombat.utils

import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CommandBuilder(private val name: String, private val description: String = "") {
    private var permission: String? = null
    private var execution: ((Player, Array<out String>) -> Unit)? = null
    private var suggest: ((Player, Array<out String>) -> List<String>)? = null

    fun permission(perm: String) = apply { this.permission = perm }
    fun execute(block: (Player, Array<out String>) -> Unit) = apply { this.execution = block }
    fun suggest(block: (Player, Array<out String>) -> List<String>) = apply { this.suggest = block }

    fun register(plugin: JavaPlugin) {
        val basicCommand = object : BasicCommand {
            override fun execute(stack: CommandSourceStack, args: Array<out String>) {
                val player = stack.sender as? Player ?: return
                execution?.invoke(player, args)
            }

            override fun suggest(stack: CommandSourceStack, args: Array<out String>): Collection<String> {
                val player = stack.sender as? Player ?: return emptyList()
                return suggest?.invoke(player, args) ?: emptyList()
            }

            override fun permission(): String? = permission
        }

        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()
            commands.register(name, description, basicCommand)
        }
    }
}