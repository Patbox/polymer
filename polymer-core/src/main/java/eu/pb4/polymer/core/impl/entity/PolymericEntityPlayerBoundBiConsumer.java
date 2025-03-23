package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PlayerBoundBiConsumer;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public record PolymericEntityPlayerBoundBiConsumer(Set<PlayerAssociatedNetworkHandler> receivers, PolymerEntity polymerEntity, BiConsumer<Packet<?>, List<UUID>> consumer)
        implements PlayerBoundBiConsumer<Packet<?>, List<UUID>> {
    public static PolymericEntityPlayerBoundBiConsumer create(Set<PlayerAssociatedNetworkHandler> listeners, PolymerEntity polymerEntity, Entity entity, BiConsumer<Packet<?>, List<UUID>> receiver) {
        return new PolymericEntityPlayerBoundBiConsumer(listeners, polymerEntity, new DirectEntityPlayerBoundBiConsumer<>(listeners, entity, receiver));
    }
    @Override
    public void accept(Packet<?> t, List<UUID> y) {
        polymerEntity.onEntityPacketSent(x -> consumer.accept(x, y), t);
    }
}
