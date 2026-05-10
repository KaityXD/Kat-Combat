package com.kaity.katcombat

import com.kaity.katcombat.commands.Command
import com.kaity.katcombat.events.*
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.Config
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

        Command(this, combat).register()

        server.pluginManager.registerEvents(OnKilled(combat), this)
        server.pluginManager.registerEvents(OnDied(combat), this)
        server.pluginManager.registerEvents(OnAttacked(combat), this)
        server.pluginManager.registerEvents(OnCommand(combat), this)
        server.pluginManager.registerEvents(OnQuit(combat), this)
        server.pluginManager.registerEvents(OnMove(combat), this)
        server.pluginManager.registerEvents(OnGlide(combat), this)
        server.pluginManager.registerEvents(OnHeadPlaceBreak(), this)

        logger.info("KatCombat has been enabled!")
    }

    override fun onDisable() {
        if (::combat.isInitialized) {
            combat.cleanup()
        }
        logger.info("KatCombat has been disabled!")
    }
}