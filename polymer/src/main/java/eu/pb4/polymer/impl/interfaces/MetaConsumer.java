package eu.pb4.polymer.impl.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.world.EntityTrackingListener;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface MetaConsumer<T, E> extends Consumer<T> {
    E getAttached();

    static MetaConsumer<Packet<?>, Collection<EntityTrackingListener>> plsFixInDevRemappingFabric(ThreadedAnvilChunkStorage.EntityTracker entityTracker, Set<EntityTrackingListener> listeners, Entity entity) {
        return new MetaConsumer<>() {
            @Override
            public Collection<EntityTrackingListener> getAttached() {
                return listeners;
            }

            @Override
            public void accept(Packet<?> packet) {
                entityTracker.sendToOtherNearbyPlayers(EntityAttachedPacket.set(packet, entity));
            }
        };
    }
}
