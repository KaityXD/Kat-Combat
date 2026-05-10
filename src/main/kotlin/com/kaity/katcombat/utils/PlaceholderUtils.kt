package com.kaity.katcombat.utils

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object PlaceholderUtils {
    private val hasPAPI: Boolean by lazy {
        Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")
    }

    fun apply(message: String, player: Player): String {
        if (!hasPAPI) return message
        return try {
            applyPAPI(message, player)
        } catch (e: Throwable) {
            message
        }
    }

    private fun applyPAPI(message: String, player: Player): String {
        return PlaceholderAPI.setPlaceholders(player, message)
    }
}