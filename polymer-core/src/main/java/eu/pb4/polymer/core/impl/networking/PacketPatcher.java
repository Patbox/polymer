package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.impl.interfaces.StatusEffectPacketExtension;
import eu.pb4.polymer.networking.api.DynamicPacket;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.ArrayList;

public class PacketPatcher {

    public static Packet<ClientPlayPacketListener> replace(ServerPlayNetworkHandler handler, Packet<ClientPlayPacketListener> packet) {
        if (packet instanceof EntityEquipmentUpdateS2CPacket original && EntityAttachedPacket.get(original, original.getId()) instanceof PolymerEntity polymerEntity) {
            return EntityAttachedPacket.setIfEmpty(
                    new EntityEquipmentUpdateS2CPacket(((Entity) polymerEntity).getId(), polymerEntity.getPolymerVisibleEquipment(original.getEquipmentList(), handler.getPlayer())),
                    (Entity) polymerEntity
            );
        }

        if (packet instanceof BundleS2CPacket bundleS2CPacket) {
            var list = new ArrayList<Packet<ClientPlayPacketListener>>();
            var iterator = bundleS2CPacket.getPackets().iterator();
            while (iterator.hasNext()) {
                var x = iterator.next();
                if (!prevent(handler, x)) {
                    list.add(replace(handler, x));
                }
            }

            return new BundleS2CPacket(list);
        }

        return packet;
    }

    public static void sendExtra(ServerPlayNetworkHandler handler, Packet<ClientPlayPacketListener> packet) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            ImmersivePortalsUtils.sendBlockPackets(handler, packet);
        }

        BlockPacketUtil.sendFromPacket(packet, handler);
    }

    public static boolean prevent(ServerPlayNetworkHandler handler, Packet<ClientPlayPacketListener> packet) {
        if ((
                (packet instanceof PlaySoundS2CPacket soundPacket && soundPacket.getSound().value() == PolymerSoundEvent.EMPTY_SOUND)
                        || packet instanceof StatusEffectPacketExtension packet2
                        && ((packet2.polymer$getStatusEffect() instanceof PolymerStatusEffect pol && pol.getPolymerReplacement(handler.player) == null))
        ) || !EntityAttachedPacket.shouldSend(packet, handler.player)
        ) {
            return true;
        }

        if ((packet instanceof EntityEquipmentUpdateS2CPacket original && original.getEquipmentList().isEmpty()) || !EntityAttachedPacket.shouldSend(packet, handler.player)) {
            return true;
        }

        return false;
    }
}
