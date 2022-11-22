package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface EntityAttachedPacket {
    Entity polymer$getEntity();
    Packet<?> polymer$setEntity(Entity entity);

    @Nullable
    static Entity get(Object packet) {
        return packet instanceof EntityAttachedPacket e ? e.polymer$getEntity() : null;
    }

    static <T> T setIfEmpty(T packet, Entity entity) {
        return packet instanceof EntityAttachedPacket e && e.polymer$getEntity() == null ? (T) e.polymer$setEntity(entity) : packet;
    }

    static <T> T set(T packet, Entity entity) {
        return packet instanceof EntityAttachedPacket e ? (T) e.polymer$setEntity(entity) : packet;
    }

    static boolean shouldSend(Packet<?> packet, ServerPlayerEntity player) {
        return packet instanceof EntityAttachedPacket e ? e.polymer$getEntity() instanceof PolymerEntity entity ? entity.sendPacketsTo(player) : true : true;
    }
}
