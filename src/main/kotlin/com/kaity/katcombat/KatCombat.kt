package com.kaity.katcombat

import com.comphenix.protocol.ProtocolLibrary
import com.kaity.katcombat.commands.Command
import com.kaity.katcombat.events.*
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Config
import com.kaity.katcombat.managers.WorldGuardHelper
import org.bukkit.plugin.java.JavaPlugin

class KatCombat : JavaPlugin() {
    lateinit var combat: Combat
    lateinit var config: Config

    companion object {
        lateinit var instance: KatCombat
            private set
    }

    override fun onEnable() {
        instance = this
        config = Config(this)
        combat = Combat(this, config)

        WorldGuardHelper.init(this)

        Command(this, combat).register()

        server.pluginManager.registerEvents(OnKilled(combat), this)
        server.pluginManager.registerEvents(OnDied(combat), this)
        server.pluginManager.registerEvents(OnAttacked(combat), this)
        server.pluginManager.registerEvents(OnCommand(combat), this)
        server.pluginManager.registerEvents(OnQuit(combat), this)
        server.pluginManager.registerEvents(OnMove(combat), this)
        server.pluginManager.registerEvents(OnGlide(combat), this)
        server.pluginManager.registerEvents(OnHeadPlaceBreak(), this)

        // Register ProtocolLib packet listener if available and configured
        if (config.attackDetectionMode == "packet") {
            if (server.pluginManager.getPlugin("ProtocolLib") != null) {
                try {
                    ProtocolLibrary.getProtocolManager()
                        .addPacketListener(AttackPacketListener(this, combat))
                    logger.info("KatCombat attack packet listener registered via ProtocolLib.")
                } catch (e: Exception) {
                    logger.warning("Failed to register ProtocolLib packet listener: ${e.message}")
                    logger.warning("Falling back to damage-based attack detection.")
                }
            } else {
                logger.warning("ProtocolLib not found but attack-detection-mode is set to 'packet'.")
                logger.warning("Falling back to damage-based attack detection.")
            }
        }

        logger.info("KatCombat has been enabled!")
    }

    override fun onDisable() {
        if (::combat.isInitialized) {
            combat.cleanup()
        }
        logger.info("KatCombat has been disabled!")
    }
}
