package com.kaity.katcombat.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player

object MessageUtils {
    private val mm = MiniMessage.miniMessage()

    fun parse(msg: String): Component = mm.deserialize(msg)

    fun Player.sendMessageMini(msg: String) {
        if (msg.isNotEmpty()) {
            this.sendMessage(parse(msg))
        }
    }

    fun Player.sendActionBarMini(msg: String) {
        if (msg.isNotEmpty()) {
            this.sendActionBar(parse(msg))
        }
    }
}