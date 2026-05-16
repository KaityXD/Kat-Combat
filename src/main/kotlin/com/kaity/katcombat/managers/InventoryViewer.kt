package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.concurrent.ConcurrentHashMap

class GuiInventoryHolder : InventoryHolder {
    override fun getInventory(): Inventory {
        return Bukkit.createInventory(this, 9)
    }
}

class InventoryViewer(private val plugin: KatCombat) : Listener {

    private val snapshots = ConcurrentHashMap<String, InventorySnapshot>()
    private val mm = MiniMessage.miniMessage()

    data class InventorySnapshot(
        val victimName: String,
        val victimUuid: String,
        val items: Array<ItemStack?>,
        val armor: Array<ItemStack?>,
        val offhand: ItemStack?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as InventorySnapshot
            if (victimName != other.victimName) return false
            if (victimUuid != other.victimUuid) return false
            if (!items.contentEquals(other.items)) return false
            if (!armor.contentEquals(other.armor)) return false
            if (offhand != other.offhand) return false
            return true
        }

        override fun hashCode(): Int {
            var result = victimName.hashCode()
            result = 31 * result + victimUuid.hashCode()
            result = 31 * result + items.contentHashCode()
            result = 31 * result + armor.contentHashCode()
            result = 31 * result + (offhand?.hashCode() ?: 0)
            return result
        }
    }

    fun createSnapshot(victim: Player): String {
        val key = java.util.UUID.randomUUID().toString().substring(0, 8)
        val inv = victim.inventory
        val snapshot = InventorySnapshot(
            victimName = victim.name,
            victimUuid = victim.uniqueId.toString(),
            items = inv.storageContents?.map { it?.clone() }?.toTypedArray() ?: emptyArray(),
            armor = inv.armorContents?.map { it?.clone() }?.toTypedArray() ?: emptyArray(),
            offhand = inv.itemInOffHand.clone()
        )
        snapshots[key] = snapshot

        plugin.server.globalRegionScheduler.runDelayed(plugin, { _ ->
            snapshots.remove(key)
        }, 5 * 60 * 20L)

        return key
    }

    fun openInventory(player: Player, key: String) {
        val snap = snapshots[key] ?: run {
            player.sendMessage(mm.deserialize("<#ff4757><b>✕</b> <gray>That snapshot expired."))
            return
        }

        val gui = Bukkit.createInventory(
            GuiInventoryHolder(),
            54,
            mm.deserialize("<#2f3542>☠ <#ff4757><b>${snap.victimName}</b><#747d8c>'s Loot")
        )

        val dark = pane(Material.BLACK_STAINED_GLASS_PANE, "<#2f3542>▪")
        val red = pane(Material.RED_STAINED_GLASS_PANE, "<#ff4757>▪")
        val orange = pane(Material.ORANGE_STAINED_GLASS_PANE, "<#ffa502>▪")
        val gray = pane(Material.GRAY_STAINED_GLASS_PANE, "<#747d8c>▪")

        // ━━━ Row 0: Header ━━━
        for (i in 0..8) gui.setItem(i, dark)
        val skull = ItemStack(Material.PLAYER_HEAD).apply {
            val meta = itemMeta as SkullMeta
            meta.owningPlayer = Bukkit.getOfflinePlayer(java.util.UUID.fromString(snap.victimUuid))
            meta.displayName(mm.deserialize("<#ff4757><b>☠ ${snap.victimName}</b>"))
            meta.lore(listOf(
                mm.deserialize("<#747d8c>Captured at time of death."),
                mm.deserialize("<#747d8c>Click items to inspect."),
                mm.deserialize(""),
                mm.deserialize("<dark_gray>Snapshot ID: ${key}")
            ))
            itemMeta = meta
        }
        gui.setItem(4, skull)

        // ━━━ Rows 1-3: Main Inventory (indices 9-35) ━━━
        var idx = 9
        for (row in 1..3) {
            for (col in 0..8) {
                val slot = row * 9 + col
                val item = snap.items.getOrNull(idx)
                if (item != null && !item.type.isAir) gui.setItem(slot, item)
                idx++
            }
        }

        // ━━━ Row 4: Hotbar (indices 0-8) ━━━
        for (col in 0..8) {
            val slot = 36 + col
            val item = snap.items.getOrNull(col)
            if (item != null && !item.type.isAir) gui.setItem(slot, item)
        }

        // ━━━ Row 5: Armor + Offhand + Labels ━━━
        // 45:Helmet, 46:Chest, 47:Legs, 48:Boots, 49:label, 50:Offhand, 51-53:decoration
        val armorSlots = listOf(45, 46, 47, 48)
        val armorIcons = listOf("⛑ Helmet", "🦺 Chest", "👖 Legs", "👢 Boots")
        snap.armor.forEachIndexed { i, item ->
            gui.setItem(armorSlots[i], item?.takeIf { !it.type.isAir } ?: gray(armorIcons[i]))
        }

        gui.setItem(49, red("<b>◆ ARMOR"))
        gui.setItem(50, snap.offhand?.takeIf { !it.type.isAir } ?: gray("✋ Offhand"))
        gui.setItem(51, orange("<b>◆ OFFHAND"))
        gui.setItem(52, dark)
        gui.setItem(53, dark)

        player.openInventory(gui)
    }

    private fun pane(mat: Material, name: String): ItemStack {
        return ItemStack(mat).apply {
            val m = itemMeta!!
            m.displayName(mm.deserialize(name))
            m.isHideTooltip = true
            itemMeta = m
        }
    }

    private fun gray(label: String) = pane(Material.GRAY_STAINED_GLASS_PANE, "<#747d8c>$label")
    private fun red(label: String) = pane(Material.RED_STAINED_GLASS_PANE, "<#ff4757>$label")
    private fun orange(label: String) = pane(Material.ORANGE_STAINED_GLASS_PANE, "<#ffa502>$label")

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.view.topInventory.holder is GuiInventoryHolder) {
            e.isCancelled = true
        }
    }
}
