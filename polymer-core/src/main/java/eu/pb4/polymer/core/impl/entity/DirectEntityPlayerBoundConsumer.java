package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Set;
import java.util.function.Consumer;

public record DirectEntityPlayerBoundConsumer<T>(Set<PlayerAssociatedNetworkHandler> receivers, Entity entity, Consumer<T> consumer) implements PlayerBoundConsumer<T> {
    @Override
    public void accept(T t) {
        consumer.accept(EntityAttachedPacket.setIfEmpty(t, entity));
    }
}
