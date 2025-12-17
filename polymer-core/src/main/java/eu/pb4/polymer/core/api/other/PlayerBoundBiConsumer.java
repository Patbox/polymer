package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.impl.entity.DirectEntityPlayerBoundBiConsumer;
import eu.pb4.polymer.core.impl.entity.DirectEntityPlayerBoundConsumer;
import eu.pb4.polymer.core.impl.entity.PolymericEntityPlayerBoundBiConsumer;
import eu.pb4.polymer.core.impl.entity.PolymericEntityPlayerBoundConsumer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public interface PlayerBoundBiConsumer<T, Y> extends BiConsumer<T, Y> {
    static PlayerBoundBiConsumer<Packet<?>, List<UUID>> createPacketFor(Set<ServerPlayerConnection> listeners, Entity entity, BiConsumer<Packet<?>, List<UUID>> receiver) {
        var polymerEntity = PolymerEntity.get(entity);
        return polymerEntity != null
                ? PolymericEntityPlayerBoundBiConsumer.create(listeners, polymerEntity, entity, receiver)
                : new DirectEntityPlayerBoundBiConsumer<>(listeners, entity, receiver);
    }

    Set<ServerPlayerConnection> receivers();
}
