package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.SimpleUpdateType;
import eu.pb4.polymer.virtualentity.impl.VoidUpdateType;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.function.Consumer;

public interface HolderAttachment {
    ElementHolder holder();
    void destroy();
    Vec3d getPos();
    ServerWorld getWorld();
    void updateCurrentlyTracking(Collection<ServerPlayNetworkHandler> currentlyTracking);
    void updateTracking(ServerPlayNetworkHandler tracking);

    default void startWatching(ServerPlayerEntity handler) {
        this.holder().startWatching(handler);
    }

    default void startWatching(ServerPlayNetworkHandler handler) {
        this.holder().startWatching(handler);
    }

    default void startWatchingExtraPackets(ServerPlayNetworkHandler handler, Consumer<Packet<ClientPlayPacketListener>> packetConsumer) {};

    default void stopWatching(ServerPlayerEntity handler) {
        this.holder().stopWatching(handler);
    }

    default void stopWatching(ServerPlayNetworkHandler handler) {
        this.holder().stopWatching(handler);
    }

    default void tick() {
        this.holder().tick();
    }

    /**
     * This shouldn't change value once added to target!
     */
    default boolean shouldTick() {
        return true;
    }


    interface UpdateType {
        UpdateType POSITION = UpdateType.of("BlockState");

        static UpdateType of() {
            return new VoidUpdateType();
        }

        static UpdateType of(String type) {
            return new SimpleUpdateType(type);
        }
    }
}
