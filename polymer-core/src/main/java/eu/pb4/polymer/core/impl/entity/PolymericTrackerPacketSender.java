package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerTrackerPacketSender;
import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.core.mixin.entity.EntityTrackerAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record PolymericTrackerPacketSender(EntityTrackerEntry.TrackerPacketSender tracker, Supplier<Set<PlayerAssociatedNetworkHandler>> listenerSupplier, Entity entity, PolymerEntity polymerEntity) implements PolymerTrackerPacketSender {
    @Override
    public Set<PlayerAssociatedNetworkHandler> listeners() {
        return listenerSupplier.get();
    }

    @Override
    public void sendToListeners(Packet<? super ClientPlayPacketListener> packet) {
        //noinspection unchecked
        polymerEntity.onEntityPacketSent(x -> this.tracker.sendToListeners((Packet<? super ClientPlayPacketListener>) EntityAttachedPacket.setIfEmpty(x, entity)), packet);
    }

    @Override
    public void sendToSelfAndListeners(Packet<? super ClientPlayPacketListener> packet) {
        //noinspection unchecked
        polymerEntity.onEntityPacketSent(x -> this.tracker.sendToSelfAndListeners((Packet<? super ClientPlayPacketListener>) EntityAttachedPacket.setIfEmpty(x, entity)), packet);
    }

    @Override
    public void sendToListenersIf(Packet<? super ClientPlayPacketListener> packet, Predicate<ServerPlayerEntity> predicate) {
        //noinspection unchecked
        polymerEntity.onEntityPacketSent(x -> this.tracker.sendToListenersIf((Packet<? super ClientPlayPacketListener>) EntityAttachedPacket.setIfEmpty(x, entity), predicate), packet);
    }
}
