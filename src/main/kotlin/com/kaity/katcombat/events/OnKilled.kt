package com.kaity.katcombat.events

import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.DeathMessageManager
import com.kaity.katcombat.managers.Effects
import com.kaity.katcombat.utils.Messages
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import java.text.SimpleDateFormat
import java.util.Date

class OnKilled(private val combat: Combat) : Listener {

    @EventHandler
    fun onKilled(e: PlayerDeathEvent) {
        val victim = e.entity
        val killer = victim.killer

        if (killer != null) {
            if (combat.isInCombat(victim) || combat.isInCombat(killer)) {
                // Snapshot is taken synchronously during PlayerDeathEvent, before the inventory is cleared.
                val inventoryKey = KatCombat.instance.inventoryViewer.createSnapshot(victim)
                val session = combat.getSession(victim)
                val deathMessage = DeathMessageManager.buildDeathMessage(killer, victim, session, inventoryKey, combat.config)
                combat.handlePlayerKilled(killer, victim, e, deathMessage)
                
                // Play death sound
                val soundName = combat.config.deathSound
                if (soundName.uppercase() != "NONE") {
                    try {
                        val key = NamespacedKey.minecraft(soundName.lowercase())
                        val sound = Registry.SOUNDS.get(key)
                        if (sound != null) {
                            victim.world.playSound(victim.location, sound, 1.0f, 1.0f)
                        }
                    } catch (ex: Exception) {
                        // Invalid sound in config, ignore
                    }
                }

                if (Effects.hasEffectEnabled(killer)) {
                    Effects.playDeathEffects(killer, victim)
                }

                if (combat.config.dropHeadEnabled) {
                    val chance = combat.config.dropHeadChance
                    if (Math.random() * 100 < chance) {
                        val head = ItemStack(Material.PLAYER_HEAD)
                        val meta = head.itemMeta as SkullMeta
                        meta.owningPlayer = victim
                        
                        val dateFormat = SimpleDateFormat("dd/MM/yy , HH:mm")
                        val date = dateFormat.format(Date())
                        
                        val lore = listOf(
                            Messages.parse("<gray>☠ killer: <white>${killer.name}"),
                            Messages.parse("<gray>⌛ Time:   <white>$date")
                        )
                        meta.lore(lore)

                        val killerKey = NamespacedKey(KatCombat.instance, "head_killer")
                        val timeKey = NamespacedKey(KatCombat.instance, "head_time")
                        meta.persistentDataContainer.set(killerKey, PersistentDataType.STRING, killer.name)
                        meta.persistentDataContainer.set(timeKey, PersistentDataType.STRING, date)

                        head.itemMeta = meta
                        
                        e.drops.add(head)
                    }
                }
            }
            combat.untagPlayer(victim)
        }
    }
}
