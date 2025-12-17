package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.impl.entity.DirectEntityPlayerBoundConsumer;
import eu.pb4.polymer.core.impl.entity.PolymericEntityPlayerBoundConsumer;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public interface PlayerBoundConsumer<T> extends Consumer<T> {
    static PlayerBoundConsumer<Packet<?>> createPacketFor(Set<ServerPlayerConnection> listeners, Entity entity, Consumer<Packet<?>> receiver) {
        var polymerEntity = PolymerEntity.get(entity);
        return polymerEntity != null
                ? PolymericEntityPlayerBoundConsumer.create(listeners, polymerEntity, entity, receiver)
                : new DirectEntityPlayerBoundConsumer<>(listeners, entity, receiver);
    }

    Set<ServerPlayerConnection> receivers();
}
