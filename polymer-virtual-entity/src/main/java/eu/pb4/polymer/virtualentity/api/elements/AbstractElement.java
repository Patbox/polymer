package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.SafeBundler;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Predicate;

public abstract class AbstractElement implements VirtualElement {
    private static final Predicate<ServerPlayerEntity> DEFAULT_VISIBILITY = p -> true;
    private ElementHolder holder;
    private Vec3d offset = Vec3d.ZERO;
    @Nullable
    private Vec3d overridePos;
    @Nullable
    protected Vec3d lastSyncedPos;
    private InteractionHandler handler = InteractionHandler.EMPTY;

    protected Predicate<ServerPlayerEntity> elementVisiblityPredicate = DEFAULT_VISIBILITY;

    @Override
    public Vec3d getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Vec3d offset) {
        this.offset = offset;
    }

    @Nullable
    public Vec3d getOverridePos() {
        return this.overridePos;
    }

    @Nullable
    public void setOverridePos(Vec3d vec3d) {
        this.overridePos = vec3d;
    }

    @Override
    public Vec3d getLastSyncedPos() {
        return this.lastSyncedPos;
    }
    public void updateLastSyncedPos() {
        this.lastSyncedPos = getCurrentPos();
    }

    @Override
    public @Nullable ElementHolder getHolder() {
        return this.holder;
    }

    @Override
    public void setHolder(ElementHolder holder) {
        this.holder = holder;
    }

    @Override
    public InteractionHandler getInteractionHandler(ServerPlayerEntity player) {
        return this.handler;
    }

    public void setInteractionHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    public final void setVisibilityPredicate(Predicate<ServerPlayerEntity> predicate) {
        if (this.elementVisiblityPredicate == predicate) {
            return;
        }
        var oldPredicate = this.elementVisiblityPredicate;
        if (this.holder != null) {
            for (var player : this.holder.getWatchingPlayers()) {
                if (oldPredicate.test(player.getPlayer()) && !predicate.test(player.getPlayer())) {
                    var x = new SafeBundler(player::sendPacket);
                    this.stopWatching(player.getPlayer(), x);
                    x.finish();
                }
            }
        }
        this.elementVisiblityPredicate = predicate;
        if (this.holder != null) {
            for (var player : this.holder.getWatchingPlayers()) {
                if (!oldPredicate.test(player.getPlayer()) && predicate.test(player.getPlayer())) {
                    var x = new SafeBundler(player::sendPacket);
                    this.startWatching(player.getPlayer(), x);
                    x.finish();
                }
            }
        }
    }

    public final Predicate<ServerPlayerEntity> getVisibilityPredicate() {
        return this.elementVisiblityPredicate;
    }

    public void sendPacket(Packet<? extends ClientPlayPacketListener> packet) {
        if (this.holder != null) {
            this.holder.sendPacket(packet, DEFAULT_VISIBILITY);
        }
    }

    public void sendPacket(Packet<? extends ClientPlayPacketListener> packet, Predicate<ServerPlayerEntity> predicate) {
        if (this.holder != null) {
            this.holder.sendPacket(packet, predicate.and(DEFAULT_VISIBILITY));
        }
    }
}
