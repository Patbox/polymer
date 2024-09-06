package eu.pb4.polymer.core.api.other;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.impl.entity.DirectEntityPlayerBoundConsumer;
import eu.pb4.polymer.core.impl.entity.PolymericEntityPlayerBoundConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public interface PlayerBoundConsumer<T> extends Consumer<T> {
    static PlayerBoundConsumer<Packet<?>> createPacketFor(Set<PlayerAssociatedNetworkHandler> listeners, Entity entity, Consumer<Packet<?>> receiver) {
        return entity instanceof PolymerEntity entity1
                ? PolymericEntityPlayerBoundConsumer.create(listeners, entity1, entity, receiver)
                : new DirectEntityPlayerBoundConsumer<>(listeners, entity, receiver);
    }

    Set<PlayerAssociatedNetworkHandler> receivers();
}
