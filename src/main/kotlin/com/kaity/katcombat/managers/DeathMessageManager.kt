package com.kaity.katcombat.managers

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import com.kaity.katcombat.models.Session

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
            val durationMs = System.currentTimeMillis() - it.combatStartTime
            formatDuration(durationMs)
        } ?: "unknown"

        val killerHealth = String.format("%.1f", killer.health)
        val killerMaxHealth = String.format("%.1f", killer.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH)?.value ?: 20.0)

        val totems = countTotems(killer.inventory)
        val potions = countPotions(killer.inventory)

        val weapon = killer.inventory.itemInMainHand
        val weaponComponent = if (weapon.type.isAir) {
            Component.text("Fists", TextColor.color(0xFFFFFF))
        } else {
            val meta = weapon.itemMeta
            if (meta != null && meta.hasDisplayName()) {
                meta.displayName() ?: Component.text(weapon.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
            } else {
                Component.text(weapon.type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
            }
        }

        val hover = Component.text()
            .append(mm.deserialize("<gradient:#ff5555:#cc0000>⚔ Combat Stats</gradient>"))
            .append(Component.newline())
            .append(mm.deserialize("<gray>Killer: <white>${killer.name}</white>"))
            .append(Component.newline())
            .append(mm.deserialize("<gray>Health: <red>${killerHealth}</red><gray>/</gray><red>${killerMaxHealth}</red> ❤"))
            .append(Component.newline())
            .append(mm.deserialize("<gray>Totems: <yellow>${totems}</yellow>"))
            .append(Component.newline())
            .append(mm.deserialize("<gray>Potions: <aqua>${potions}</aqua>"))
            .append(Component.newline())
            .append(Component.text("Weapon: ", TextColor.color(0xAAAAAA)))
            .append(weaponComponent)
            .append(Component.newline())
            .append(mm.deserialize("<gray>Combat Duration: <green>${combatDuration}</green>"))
            .append(Component.newline())
            .append(mm.deserialize("<dark_gray>Click to view inventory"))
            .build()

        val baseMessage = config.getMessage("player-killed")
            .replace("<killer>", killer.name)
            .replace("<player>", victim.name)

        return mm.deserialize(baseMessage)
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(ClickEvent.runCommand("/kcview $inventoryKey"))
    }

    private fun countTotems(inv: org.bukkit.inventory.PlayerInventory): Int {
        val storage = inv.storageContents?.filterNotNull()?.filter { it.type == Material.TOTEM_OF_UNDYING }?.sumOf { it.amount } ?: 0
        val armor = inv.armorContents?.filterNotNull()?.filter { it.type == Material.TOTEM_OF_UNDYING }?.sumOf { it.amount } ?: 0
        val offhand = if (inv.itemInOffHand.type == Material.TOTEM_OF_UNDYING) inv.itemInOffHand.amount else 0
        return storage + armor + offhand
    }

    private fun countPotions(inv: org.bukkit.inventory.PlayerInventory): Int {
        val types = setOf(Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION)
        val storage = inv.storageContents?.filterNotNull()?.filter { it.type in types }?.sumOf { it.amount } ?: 0
        val armor = inv.armorContents?.filterNotNull()?.filter { it.type in types }?.sumOf { it.amount } ?: 0
        val offhand = if (inv.itemInOffHand.type in types) inv.itemInOffHand.amount else 0
        return storage + armor + offhand
    }

    private fun formatDuration(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${remainingSeconds}s"
        }
    }
}
