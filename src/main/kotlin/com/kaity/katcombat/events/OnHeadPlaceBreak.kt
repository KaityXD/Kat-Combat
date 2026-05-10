package com.kaity.katcombat.events

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.utils.Messages
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Skull
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType

class OnHeadPlaceBreak : Listener {

    private val killerKey = NamespacedKey(KatCombat.instance, "head_killer")
    private val timeKey = NamespacedKey(KatCombat.instance, "head_time")

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val item = e.itemInHand
        if (item.type != Material.PLAYER_HEAD) return

        val itemMeta = item.itemMeta ?: return
        val pdc = itemMeta.persistentDataContainer

        if (pdc.has(killerKey, PersistentDataType.STRING) && pdc.has(timeKey, PersistentDataType.STRING)) {
            val killer = pdc.get(killerKey, PersistentDataType.STRING)
            val time = pdc.get(timeKey, PersistentDataType.STRING)

            val state = e.blockPlaced.state
            if (state is Skull) {
                if (killer != null) state.persistentDataContainer.set(killerKey, PersistentDataType.STRING, killer)
                if (time != null) state.persistentDataContainer.set(timeKey, PersistentDataType.STRING, time)
                state.update()
            }
        }
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val state = e.block.state
        if (state !is Skull) return

        val pdc = state.persistentDataContainer
        if (pdc.has(killerKey, PersistentDataType.STRING) && pdc.has(timeKey, PersistentDataType.STRING)) {
            val killer = pdc.get(killerKey, PersistentDataType.STRING)
            val time = pdc.get(timeKey, PersistentDataType.STRING)

            if (killer != null && time != null) {
                e.isDropItems = false
                
                val drop = ItemStack(Material.PLAYER_HEAD)
                val meta = drop.itemMeta as SkullMeta
                meta.owningPlayer = state.owningPlayer
                
                val lore = listOf(
                    Messages.parse("<gray>☠ killer: <white>$killer"),
                    Messages.parse("<gray>⌛ Time:   <white>$time")
                )
                meta.lore(lore)
                
                meta.persistentDataContainer.set(killerKey, PersistentDataType.STRING, killer)
                meta.persistentDataContainer.set(timeKey, PersistentDataType.STRING, time)
                
                drop.itemMeta = meta
                
                e.block.world.dropItemNaturally(e.block.location, drop)
            }
        }
    }
}
