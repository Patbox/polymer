package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.mixin.entity.EntityAttributesS2CPacketMixin;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface EntityAttachedPacket {
    @Nullable
    static Entity get(Object packet, int entityId) {
        var entity = get(packet);
        return entity != null && entity.getId() == entityId ? entity : null;
    }

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
        var entity = get(packet);
        var x = entity instanceof PolymerEntity;
        return !x || (x && ((PolymerEntity) entity).sendPacketsTo(player));
    }
}
