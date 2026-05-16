package com.kaity.katcombat.managers

import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

object WorldGuardHelper {
    private var worldGuardEnabled = false

    fun init(plugin: JavaPlugin) {
        worldGuardEnabled = plugin.server.pluginManager.getPlugin("WorldGuard") != null
    }

    fun isEnabled(): Boolean = worldGuardEnabled

    fun isPvpAllowed(location: Location): Boolean {
        if (!worldGuardEnabled) return true

        return try {
            val container = com.sk89q.worldguard.WorldGuard.getInstance().platform.regionContainer
            val regionManager = container.get(com.sk89q.worldguard.bukkit.BukkitAdapter.adapt(location.world))
                ?: return true
            val set = regionManager.getApplicableRegions(com.sk89q.worldguard.bukkit.BukkitAdapter.adapt(location))
            val state = set.queryValue(null, com.sk89q.worldguard.protection.flags.Flags.PVP)
            state != com.sk89q.worldguard.protection.flags.StateFlag.State.DENY
        } catch (e: Exception) {
            true
        }
    }
}
