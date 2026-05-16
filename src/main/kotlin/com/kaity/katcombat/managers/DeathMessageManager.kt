package com.kaity.katcombat.managers

import com.kaity.katcombat.models.Session
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object DeathMessageManager {

    private val mm = MiniMessage.miniMessage()

    fun buildDeathMessage(
        killer: Player,
        victim: Player,
        session: Session?,
        inventoryKey: String,
        config: Config
    ): Component {
        val combatDuration = session?.let {
            formatDuration(System.currentTimeMillis() - it.combatStartTime)
        } ?: "?"

        val maxHealth = killer.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        val healthBar = buildHealthBar(killer.health, maxHealth)

        val totems = countTotems(killer.inventory)
        val potions = countPotions(killer.inventory)
        val weapon = formatWeapon(killer.inventory.itemInMainHand)

        val hover = Component.text()
            .append(mm.deserialize("<#ff4757><b>╍╍╍ COMBAT REPORT ╍╍╍"))
            .append(Component.newline())
            .append(Component.newline())
            .append(mm.deserialize("<#747d8c>Killer: <#ffffff><b>${killer.name}</b>"))
            .append(Component.newline())
            .append(mm.deserialize("<#747d8c>Health: ")).append(healthBar)
            .append(Component.newline())
            .append(Component.newline())
            .append(mm.deserialize(" <#ffa502>🛡 Totems: <#ffffff><b>$totems</b>"))
            .append(Component.newline())
            .append(mm.deserialize(" <#2ed573>⚗ Potions: <#ffffff><b>$potions</b>"))
            .append(Component.newline())
            .append(mm.deserialize(" <#eccc68>⚔ Weapon: ")).append(weapon)
            .append(Component.newline())
            .append(Component.newline())
            .append(mm.deserialize("<#747d8c>Combat Duration: <#2ed573><b>$combatDuration</b>"))
            .append(Component.newline())
            .append(Component.newline())
            .append(mm.deserialize("<#57606f>➤ Click to view inventory"))
            .build()

        val baseMessage = config.getMessage("player-killed")
            .replace("<killer>", killer.name)
            .replace("<player>", victim.name)

        return mm.deserialize(baseMessage)
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(ClickEvent.runCommand("/kcview $inventoryKey"))
    }

    private fun buildHealthBar(current: Double, max: Double): Component {
        val filled = ((current / max) * 10).toInt().coerceIn(0, 10)
        val empty = 10 - filled
        val bar = buildString {
            append("<#ff4757>")
            repeat(filled) { append("❤") }
            append("<#2f3542>")
            repeat(empty) { append("❤") }
        }
        return mm.deserialize("$bar <#747d8c>${"%.1f".format(current)}<dark_gray>/<#747d8c>${"%.1f".format(max)}")
    }

    private fun formatWeapon(weapon: ItemStack): Component {
        if (weapon.type.isAir) {
            return mm.deserialize("<#ffffff><b>Fists</b> <#747d8c>(bare handed)")
        }

        val meta = weapon.itemMeta
        val name = if (meta != null && meta.hasDisplayName() && meta.displayName() != null) {
            meta.displayName()!!
        } else {
            val pretty = weapon.type.name.replace("_", " ").lowercase()
                .replaceFirstChar { it.uppercase() }
            Component.text(pretty, TextColor.color(0xFFFFFF))
        }

        val durability = (meta as? Damageable)?.let {
            val max = weapon.type.maxDurability.toInt()
            val left = max - it.damage
            if (max > 0) " <#747d8c>($left<dark_gray>/<#747d8c>$max)" else ""
        } ?: ""

        val enchants = if (meta != null && meta.hasEnchants()) {
            val list = meta.enchants.map { (ench, lvl) ->
                "<#a29bfe>${ench.key.key.replace("_", " ").lowercase()} <#ffffff>$lvl"
            }.joinToString(", ")
            " <#57606f>[$list<#57606f>]"
        } else ""

        return Component.text()
            .append(name)
            .append(mm.deserialize(durability))
            .append(mm.deserialize(enchants))
            .build()
    }

    private fun countTotems(inv: org.bukkit.inventory.PlayerInventory): Int {
        val types = setOf(Material.TOTEM_OF_UNDYING)
        return countItems(inv, types)
    }

    private fun countPotions(inv: org.bukkit.inventory.PlayerInventory): Int {
        val types = setOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
        return countItems(inv, types)
    }

    private fun countItems(inv: org.bukkit.inventory.PlayerInventory, types: Set<Material>): Int {
        val storage = inv.storageContents?.filterNotNull()?.filter { it.type in types }?.sumOf { it.amount } ?: 0
        val armor = inv.armorContents?.filterNotNull()?.filter { it.type in types }?.sumOf { it.amount } ?: 0
        val offhand = if (inv.itemInOffHand.type in types) inv.itemInOffHand.amount else 0
        return storage + armor + offhand
    }

    private fun formatDuration(ms: Long): String {
        val s = ms / 1000
        val m = s / 60
        val rs = s % 60
        return when {
            m > 0 -> "${m}m ${rs}s"
            else -> "${rs}s"
        }
    }
}
