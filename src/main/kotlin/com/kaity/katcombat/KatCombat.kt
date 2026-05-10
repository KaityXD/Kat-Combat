package com.kaity.katcombat

import com.kaity.katcombat.commands.CombatCommand
import com.kaity.katcombat.events.*
import com.kaity.katcombat.managers.CombatManager
import com.kaity.katcombat.managers.ConfigManager
import org.bukkit.plugin.java.JavaPlugin

class KatCombat : JavaPlugin() {
    lateinit var combatManager: CombatManager
    lateinit var configManager: ConfigManager

    companion object {
        lateinit var instance: KatCombat
            private set
    }

    override fun onEnable() {
        instance = this
        configManager = ConfigManager(this)
        combatManager = CombatManager(this, configManager)

        CombatCommand(this, combatManager).register()

        server.pluginManager.registerEvents(OnKilled(combatManager), this)
        server.pluginManager.registerEvents(OnDied(combatManager), this)
        server.pluginManager.registerEvents(OnAttacked(combatManager), this)
        server.pluginManager.registerEvents(OnCommand(combatManager), this)
        server.pluginManager.registerEvents(OnQuit(combatManager), this)
        server.pluginManager.registerEvents(OnMove(combatManager), this)
        server.pluginManager.registerEvents(OnToggleGlide(combatManager), this)

        logger.info("KatCombat has been enabled!")
    }

    override fun onDisable() {
        if (::combatManager.isInitialized) {
            combatManager.cleanup()
        }
        logger.info("KatCombat has been disabled!")
    }
}