package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.impl.SimpleUpdateType;
import eu.pb4.polymer.virtualentity.impl.VoidUpdateType;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public interface HolderAttachment {
    ElementHolder holder();
    void destroy();
    Vec3 getPos();
    ServerLevel getWorld();
    void updateCurrentlyTracking(Collection<ServerGamePacketListenerImpl> currentlyTracking);
    void updateTracking(ServerGamePacketListenerImpl tracking);

    default boolean isRemoved() {
        return false;
    }

    default void startWatching(ServerPlayer handler) {
        if (this.holder().getAttachment() == this) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                VirtualEntityUtils.wrapCallWithContext(this.getWorld(), () -> this.holder().startWatching(handler));
            } else {
                this.holder().startWatching(handler);
            }
        }
    }

    default void startWatching(ServerGamePacketListenerImpl handler) {
        if (this.holder().getAttachment() == this) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                VirtualEntityUtils.wrapCallWithContext(this.getWorld(), () -> this.holder().startWatching(handler));
            } else {
                this.holder().startWatching(handler);
            }
        }
    }

    default void startWatchingExtraPackets(ServerGamePacketListenerImpl handler, Consumer<Packet<ClientGamePacketListener>> packetConsumer) {};

    default void stopWatching(ServerPlayer handler) {
        if (this.holder().getAttachment() == this) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                VirtualEntityUtils.wrapCallWithContext(this.getWorld(), () -> this.holder().stopWatching(handler));
            } else {
                this.holder().stopWatching(handler);
            }
        }
    }

    default void stopWatching(ServerGamePacketListenerImpl handler) {
        if (this.holder().getAttachment() == this) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                VirtualEntityUtils.wrapCallWithContext(this.getWorld(), () -> this.holder().stopWatching(handler));
            } else {
                this.holder().stopWatching(handler);
            }
        }
    }

    default void tick() {
        if (this.holder().getAttachment() == this) {
            if (CompatStatus.IMMERSIVE_PORTALS) {
                VirtualEntityUtils.wrapCallWithContext(this.getWorld(), () -> this.holder().tick());
            } else {
                this.holder().tick();
            }
        }
    }

    /**
     * This shouldn't change value once added to target!
     */
    default boolean shouldTick() {
        return true;
    }

    default boolean canUpdatePosition() {
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
