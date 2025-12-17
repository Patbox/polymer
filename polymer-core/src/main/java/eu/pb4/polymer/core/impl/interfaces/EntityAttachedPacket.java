package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

    static boolean shouldSend(Packet<?> packet, ServerPlayer player) {
        var x = PolymerEntity.get(get(packet));
        return x == null || x.sendPacketsTo(player);
    }
}
