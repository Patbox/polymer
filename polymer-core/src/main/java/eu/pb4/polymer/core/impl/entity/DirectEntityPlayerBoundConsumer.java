package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public record DirectEntityPlayerBoundConsumer<T>(Set<ServerPlayerConnection> receivers, Entity entity, Consumer<T> consumer) implements PlayerBoundConsumer<T> {
    @Override
    public void accept(T t) {
        consumer.accept(EntityAttachedPacket.setIfEmpty(t, entity));
    }
}
