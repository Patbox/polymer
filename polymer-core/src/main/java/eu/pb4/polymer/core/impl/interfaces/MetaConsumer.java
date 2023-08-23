package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface MetaConsumer<T, E> extends Consumer<T> {
    E getAttached();

    static MetaConsumer<Packet<?>, Collection<EntityTrackingListener>> sendToOtherPlayers(ThreadedAnvilChunkStorage.EntityTracker entityTracker, Set<EntityTrackingListener> listeners, Entity entity) {
        if (entity instanceof PolymerEntity polymerEntity) {
            return new MetaConsumer<>() {
                private final Consumer<Packet<?>> consumer = entityTracker::sendToOtherNearbyPlayers;

                @Override
                public Collection<EntityTrackingListener> getAttached() {
                    return listeners;
                }

                @Override
                public void accept(Packet<?> packet) {
                    polymerEntity.onEntityPacketSent(consumer, EntityAttachedPacket.setIfEmpty(packet, entity));
                }
            };
        } else {
            return new MetaConsumer<>() {
                @Override
                public Collection<EntityTrackingListener> getAttached() {
                    return listeners;
                }

                @Override
                public void accept(Packet<?> packet) {
                    entityTracker.sendToOtherNearbyPlayers(EntityAttachedPacket.setIfEmpty(packet, entity));
                }
            };
        }
    }
}
