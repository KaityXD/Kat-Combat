package com.kaity.katcombat.models

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.boss.BossBar

data class Session(
    var endTime: Long,
    var killer: String?,
    var combatStartTime: Long = System.currentTimeMillis(),
    var actionBarTask: ScheduledTask? = null,
    var bossBarTask: ScheduledTask? = null,
    var bossBar: BossBar? = null,
    var elytraStartLocation: Location? = null,
    var originalFlightAllowed: Boolean = false,
    var originalFlying: Boolean = false,
    var originalGameMode: GameMode = GameMode.SURVIVAL
) {
    fun getRemainingTime(): Long {
        val remaining = endTime - System.currentTimeMillis()
        return if (remaining > 0) (remaining + 999) / 1000 else 0
    }
}
