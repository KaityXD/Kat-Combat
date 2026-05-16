package com.kaity.katcombat.events

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction
import com.kaity.katcombat.KatCombat
import com.kaity.katcombat.managers.Combat
import com.kaity.katcombat.managers.WorldGuardHelper
import org.bukkit.entity.Player

class AttackPacketListener(private val plugin: KatCombat, private val combat: Combat) :
    PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY) {

    override fun onPacketReceiving(event: PacketEvent) {
        val player = event.player
        val packet = event.packet

        val action = try {
            packet.enumEntityUseActions.read(0)
        } catch (e: Exception) {
            return
        }

        if (action.action != EntityUseAction.ATTACK) {
            return
        }

        val entityId = try {
            packet.integers.read(0)
        } catch (e: Exception) {
            return
        }

        val target = player.world.entities.find { it.entityId == entityId } as? Player ?: return
        if (target == player) return

        if (!WorldGuardHelper.isPvpAllowed(player.location) || !WorldGuardHelper.isPvpAllowed(target.location)) {
            return
        }

        player.scheduler.run(plugin, { _ ->
            combat.tagPlayer(player, target)
            combat.tagPlayer(target, player)
        }, null)
    }
}
