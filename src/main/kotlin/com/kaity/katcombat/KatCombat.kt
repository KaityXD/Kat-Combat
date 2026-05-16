package com.kaity.katcombat

import com.comphenix.protocol.ProtocolLibrary
import com.kaity.katcombat.commands.Command
import com.kaity.katcombat.events.*
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Config
import com.kaity.katcombat.managers.InventoryViewer
import com.kaity.katcombat.managers.WorldGuardHelper
import com.kaity.katcombat.utils.KatCombatExpansion
import org.bukkit.plugin.java.JavaPlugin

class KatCombat : JavaPlugin() {
    lateinit var combat: Combat
    lateinit var pluginConfig: Config
    lateinit var inventoryViewer: InventoryViewer

    companion object {
        lateinit var instance: KatCombat
            private set
    }

    override fun onEnable() {
        instance = this
        pluginConfig = Config(this)
        combat = Combat(this, pluginConfig)
        inventoryViewer = InventoryViewer(this)

        WorldGuardHelper.init(this)

        Command(this, combat).register()

        server.pluginManager.registerEvents(inventoryViewer, this)
        server.pluginManager.registerEvents(OnKilled(combat), this)
        server.pluginManager.registerEvents(OnDied(combat), this)
        server.pluginManager.registerEvents(OnAttacked(combat), this)
        server.pluginManager.registerEvents(OnCommand(combat), this)
        server.pluginManager.registerEvents(OnQuit(combat), this)
        server.pluginManager.registerEvents(OnJoin(combat), this)
        server.pluginManager.registerEvents(OnMove(combat), this)
        server.pluginManager.registerEvents(OnGlide(combat), this)
        server.pluginManager.registerEvents(OnHeadPlaceBreak(), this)
        server.pluginManager.registerEvents(OnTeleport(combat), this)
        server.pluginManager.registerEvents(OnDropItem(combat), this)
        server.pluginManager.registerEvents(OnFish(combat), this)

        // Register ProtocolLib packet listener if available and configured
        if (pluginConfig.attackDetectionMode == "packet") {
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

        // Register PlaceholderAPI expansion
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            try {
                KatCombatExpansion(this).register()
                logger.info("KatCombat PlaceholderAPI expansion registered.")
            } catch (e: Exception) {
                logger.warning("Failed to register PlaceholderAPI expansion: ${e.message}")
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
