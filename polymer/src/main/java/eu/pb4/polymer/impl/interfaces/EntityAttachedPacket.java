package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.mixin.entity.EntitySpawnS2CPacketMixin;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EntityAttachedPacket {
    Entity polymer_getEntity();
    Packet<?> polymer_setEntity(Entity entity);

    static Entity get(Object packet) {
        return packet instanceof EntityAttachedPacket e ? e.polymer_getEntity() : null;
    }

    static <T> T set(T packet, Entity entity) {
        return packet instanceof EntityAttachedPacket e ? (T) e.polymer_setEntity(entity) : packet;
    }

    static boolean shouldSend(Packet<?> packet, ServerPlayerEntity player) {
        return packet instanceof EntityAttachedPacket e ? e.polymer_getEntity() instanceof PolymerEntity entity ? entity.sendPacketsTo(player) : true : true;
    }
}
