package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerServerEntitySynchronizer;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;

public record DirectServerEntitySynchronizer(ServerEntity.Synchronizer tracker, Supplier<Set<ServerPlayerConnection>> listenerSupplier, Entity entity) implements PolymerServerEntitySynchronizer {
    @Override
    public Set<ServerPlayerConnection> listeners() {
        return listenerSupplier.get();
    }

    @Override
    public void sendToTrackingPlayers(Packet<? super ClientGamePacketListener> packet) {
        this.tracker.sendToTrackingPlayers(EntityAttachedPacket.setIfEmpty(packet, entity));
    }

    @Override
    public void sendToTrackingPlayersAndSelf(Packet<? super ClientGamePacketListener> packet) {
        this.tracker.sendToTrackingPlayersAndSelf(EntityAttachedPacket.setIfEmpty(packet, entity));
    }

    @Override
    public void sendToTrackingPlayersFiltered(Packet<? super ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
        this.tracker.sendToTrackingPlayersFiltered(EntityAttachedPacket.setIfEmpty(packet, entity), predicate);
    }
}
