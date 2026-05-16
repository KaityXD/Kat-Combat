package com.kaity.katcombat.utils

import com.kaity.katcombat.KatCombat
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class KatCombatExpansion(private val plugin: KatCombat) : PlaceholderExpansion() {

    override fun getIdentifier(): String = "katcombat"

    override fun getAuthor(): String = plugin.description.authors.firstOrNull() ?: "Kaity"

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        if (player == null) return null
        val combat = plugin.combat

        return when (params.lowercase()) {
            "status" -> if (combat.isInCombat(player)) "In Combat" else "Safe"
            "time" -> combat.getRemainingTime(player).toString()
            "killer" -> combat.getKiller(player) ?: "None"
            else -> null
        }
    }
}
