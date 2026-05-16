package com.kaity.katcombat.managers

import com.kaity.katcombat.KatCombat
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

class GuiInventoryHolder : InventoryHolder {
    override fun getInventory(): Inventory {
        throw UnsupportedOperationException()
    }
}

class InventoryViewer(private val plugin: KatCombat) : Listener {

    private val snapshots = ConcurrentHashMap<String, InventorySnapshot>()
    private val mm = MiniMessage.miniMessage()

    data class InventorySnapshot(
        val victimName: String,
        val items: Array<ItemStack?>,
        val armor: Array<ItemStack?>,
        val offhand: ItemStack?
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as InventorySnapshot
            if (victimName != other.victimName) return false
            if (!items.contentEquals(other.items)) return false
            if (!armor.contentEquals(other.armor)) return false
            if (offhand != other.offhand) return false
            return true
        }

        override fun hashCode(): Int {
            var result = victimName.hashCode()
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
        val snapshot = snapshots[key] ?: run {
            player.sendMessage(mm.deserialize("<red>That inventory snapshot has expired."))
            return
        }

        val size = 54
        val title = mm.deserialize("<dark_red>☠ <red>${snapshot.victimName}'s Inventory")
        val inventory = Bukkit.createInventory(GuiInventoryHolder(), size, title)

        // Main inventory rows 1-3 (indices 9-35) -> slots 0-26
        for (i in 9..35) {
            val item = snapshot.items.getOrNull(i)
            if (item != null && !item.type.isAir) {
                inventory.setItem(i - 9, item)
            }
        }

        // Hotbar (indices 0-8) -> slots 27-35
        for (i in 0..8) {
            val item = snapshot.items.getOrNull(i)
            if (item != null && !item.type.isAir) {
                inventory.setItem(27 + i, item)
            }
        }

        // Armor -> slots 36-39
        snapshot.armor.forEachIndexed { index, item ->
            if (item != null && !item.type.isAir) {
                inventory.setItem(36 + index, item)
            }
        }

        // Offhand -> slot 40
        snapshot.offhand?.let {
            if (!it.type.isAir) {
                inventory.setItem(40, it)
            }
        }

        player.openInventory(inventory)
    }

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.view.topInventory.holder is GuiInventoryHolder) {
            e.isCancelled = true
        }
    }
}
