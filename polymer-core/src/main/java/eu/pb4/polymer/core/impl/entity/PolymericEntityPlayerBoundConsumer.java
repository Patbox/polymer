package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public record PolymericEntityPlayerBoundConsumer(Set<PlayerAssociatedNetworkHandler> receivers, PolymerEntity polymerEntity, Consumer<Packet<?>> consumer)
        implements PlayerBoundConsumer<Packet<?>> {
    public static PolymericEntityPlayerBoundConsumer create(Set<PlayerAssociatedNetworkHandler> listeners, PolymerEntity polymerEntity, Entity entity, Consumer<Packet<?>> receiver) {
        return new PolymericEntityPlayerBoundConsumer(listeners, polymerEntity, new DirectEntityPlayerBoundConsumer<>(listeners, entity, receiver));
    }
    @Override
    public void accept(Packet<?> t) {
        polymerEntity.onEntityPacketSent(consumer, t);
    }
}
