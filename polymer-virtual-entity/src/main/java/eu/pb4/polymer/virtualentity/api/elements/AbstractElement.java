package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.SafeBundler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Predicate;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractElement implements VirtualElement {
    private static final Predicate<ServerPlayer> DEFAULT_VISIBILITY = p -> true;
    private ElementHolder holder;
    private Vec3 offset = Vec3.ZERO;
    @Nullable
    private Vec3 overridePos;
    @Nullable
    protected Vec3 lastSyncedPos;
    private InteractionHandler handler = InteractionHandler.EMPTY;

    protected Predicate<ServerPlayer> elementVisiblityPredicate = DEFAULT_VISIBILITY;

    @Override
    public Vec3 getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(Vec3 offset) {
        this.offset = offset;
    }

    @Nullable
    public Vec3 getOverridePos() {
        return this.overridePos;
    }

    @Nullable
    public void setOverridePos(Vec3 vec3d) {
        this.overridePos = vec3d;
    }

    @Override
    public Vec3 getLastSyncedPos() {
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
    public InteractionHandler getInteractionHandler(ServerPlayer player) {
        return this.handler;
    }

    public void setInteractionHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    public final void setVisibilityPredicate(Predicate<ServerPlayer> predicate) {
        if (this.elementVisiblityPredicate == predicate) {
            return;
        }
        var oldPredicate = this.elementVisiblityPredicate;
        if (this.holder != null) {
            for (var player : this.holder.getWatchingPlayers()) {
                if (oldPredicate.test(player.getPlayer()) && !predicate.test(player.getPlayer())) {
                    var x = new SafeBundler(player::send);
                    this.stopWatching(player.getPlayer(), x);
                    x.finish();
                }
            }
        }
        this.elementVisiblityPredicate = predicate;
        if (this.holder != null) {
            for (var player : this.holder.getWatchingPlayers()) {
                if (!oldPredicate.test(player.getPlayer()) && predicate.test(player.getPlayer())) {
                    var x = new SafeBundler(player::send);
                    this.startWatching(player.getPlayer(), x);
                    x.finish();
                }
            }
        }
    }

    public final Predicate<ServerPlayer> getVisibilityPredicate() {
        return this.elementVisiblityPredicate;
    }

    public void sendPacket(Packet<? extends ClientGamePacketListener> packet) {
        if (this.holder != null) {
            this.holder.sendPacket(packet, DEFAULT_VISIBILITY);
        }
    }

    public void sendPacket(Packet<? extends ClientGamePacketListener> packet, Predicate<ServerPlayer> predicate) {
        if (this.holder != null) {
            this.holder.sendPacket(packet, predicate.and(DEFAULT_VISIBILITY));
        }
    }
}
