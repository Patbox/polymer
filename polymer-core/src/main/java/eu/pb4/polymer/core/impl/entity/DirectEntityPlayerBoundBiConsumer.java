package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.other.PlayerBoundBiConsumer;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record DirectEntityPlayerBoundBiConsumer<T, Y>(Set<PlayerAssociatedNetworkHandler> receivers, Entity entity, BiConsumer<T, Y> consumer) implements PlayerBoundBiConsumer<T, Y> {
    @Override
    public void accept(T t, Y y) {
        consumer.accept(EntityAttachedPacket.setIfEmpty(t, entity), y);
    }
}
