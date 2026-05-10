package com.kaity.katcombat.managers

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class Config(private val plugin: JavaPlugin) {
    private var config: FileConfiguration = plugin.config

    init {
        plugin.saveDefaultConfig()
        config = plugin.config
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config
    }

    val combatDuration: Int
        get() = config.getInt("combat-duration", 15)

    val broadcastDeaths: Boolean
        get() = config.getBoolean("settings.broadcast-deaths", true)

    val logToConsole: Boolean
        get() = config.getBoolean("settings.log-to-console", true)

    val preventDisconnect: Boolean
        get() = config.getBoolean("settings.prevent-disconnect", true)

    val showActionbarTimer: Boolean
        get() = config.getBoolean("settings.show-actionbar-timer", true)
        
    val disableFlight: Boolean
        get() = config.getBoolean("settings.disable-flight", true)
        
    val forceSurvival: Boolean
        get() = config.getBoolean("settings.force-survival", true)
        
    val killOnElytraDistance: Double
        get() = config.getDouble("settings.kill-on-elytra-distance", 120.0)
        
    val deathSound: String
        get() = config.getString("settings.death-sound", "ENTITY_LIGHTNING_BOLT_THUNDER") ?: "ENTITY_LIGHTNING_BOLT_THUNDER"

    val whitelistMode: Boolean
        get() = config.getBoolean("whitelist-mode", false)

    val blockedCommands: List<String>
        get() = config.getStringList("blocked-commands").map { it.lowercase() }

    val allowedCommands: List<String>
        get() = config.getStringList("allowed-commands").map { it.lowercase() }

    val dropHeadEnabled: Boolean
        get() = config.getBoolean("drophead.enable", true)

    val dropHeadChance: Double
        get() = config.getDouble("drophead.chance", 6.0)

    fun getMessage(path: String): String {
        return config.getString("messages.$path", "") ?: ""
    }

    fun isCommandBlocked(command: String): Boolean {
        val cmd = command.lowercase()
        return if (whitelistMode) {
            cmd !in allowedCommands
        } else {
            cmd in blockedCommands
        }
    }
}
