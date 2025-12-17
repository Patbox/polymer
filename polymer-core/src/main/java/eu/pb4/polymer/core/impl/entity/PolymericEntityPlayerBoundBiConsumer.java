package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.other.PlayerBoundBiConsumer;
import eu.pb4.polymer.core.api.other.PlayerBoundConsumer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public record PolymericEntityPlayerBoundBiConsumer(Set<ServerPlayerConnection> receivers, PolymerEntity polymerEntity, BiConsumer<Packet<?>, List<UUID>> consumer)
        implements PlayerBoundBiConsumer<Packet<?>, List<UUID>> {
    public static PolymericEntityPlayerBoundBiConsumer create(Set<ServerPlayerConnection> listeners, PolymerEntity polymerEntity, Entity entity, BiConsumer<Packet<?>, List<UUID>> receiver) {
        return new PolymericEntityPlayerBoundBiConsumer(listeners, polymerEntity, new DirectEntityPlayerBoundBiConsumer<>(listeners, entity, receiver));
    }
    @Override
    public void accept(Packet<?> t, List<UUID> y) {
        polymerEntity.onEntityPacketSent(x -> consumer.accept(x, y), t);
    }
}
