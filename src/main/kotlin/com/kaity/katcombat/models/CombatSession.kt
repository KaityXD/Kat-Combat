package com.kaity.katcombat.models

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.entity.Player

data class CombatSession(
    var endTime: Long,
    var killer: String?,
    var actionBarTask: ScheduledTask? = null,
    var elytraStartLocation: Location? = null,
    var originalFlightAllowed: Boolean = false,
    var originalFlying: Boolean = false
) {
    fun getRemainingTime(): Long {
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) (remaining + 999) / 1000 else 0
    }
}