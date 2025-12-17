package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public record PolymericEntityPlayerBoundConsumer(Set<ServerPlayerConnection> receivers, PolymerEntity polymerEntity, Consumer<Packet<?>> consumer)
        implements PlayerBoundConsumer<Packet<?>> {
    public static PolymericEntityPlayerBoundConsumer create(Set<ServerPlayerConnection> listeners, PolymerEntity polymerEntity, Entity entity, Consumer<Packet<?>> receiver) {
        return new PolymericEntityPlayerBoundConsumer(listeners, polymerEntity, new DirectEntityPlayerBoundConsumer<>(listeners, entity, receiver));
    }
    @Override
    public void accept(Packet<?> t) {
        polymerEntity.onEntityPacketSent(consumer, t);
    }
}
