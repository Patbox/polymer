package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.compat.ImmersivePortalsUtils;
import eu.pb4.polymer.virtualentity.mixin.block.WorldChunkAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkAttachment implements HolderAttachment {
    private final ElementHolder holder;
    private final LevelChunk chunk;
    protected Vec3 pos;
    private final boolean autoTick;
    private volatile boolean removed = false;

    public ChunkAttachment(ElementHolder holder, LevelChunk chunk, Vec3 position, boolean autoTick) {
        this.chunk = chunk;
        this.pos = position;
        this.holder = holder;
        this.autoTick = autoTick;
        this.attach();
    }

    protected void attach() {
        ((HolderAttachmentHolder) chunk).polymerVE$addHolder(this);
        this.holder.setAttachment(this);
    }

    public static HolderAttachment of(ElementHolder holder, ServerLevel world, BlockPos pos) {
        return of(holder, world, Vec3.atCenterOf(pos));
    }

    public static HolderAttachment ofTicking(ElementHolder holder, ServerLevel world, BlockPos pos) {
        return ofTicking(holder, world, Vec3.atCenterOf(pos));
    }

    public static HolderAttachment of(ElementHolder holder, ServerLevel world, Vec3 pos) {
        var chunk = world.getChunk(BlockPos.containing(pos));

        if (chunk instanceof LevelChunk chunk1) {
            return new ChunkAttachment(holder, chunk1, pos, false);
        } else {
            CommonImpl.LOGGER.warn("Some mod tried to attach to chunk at " + BlockPos.containing(pos).toShortString() + ", but it isn't loaded!", new NullPointerException());
            return new ManualAttachment(holder, world, () -> pos);
        }
    }

    public static HolderAttachment ofTicking(ElementHolder holder, ServerLevel world, Vec3 pos) {
        var chunk = world.getChunk(BlockPos.containing(pos));

        if (chunk instanceof LevelChunk chunk1) {
            return new ChunkAttachment(holder, chunk1, pos, true);
        } else {
            CommonImpl.LOGGER.warn("Some mod tried to attach to chunk at " + BlockPos.containing(pos).toShortString() + ", but it isn't loaded!", new NullPointerException());
            return new ManualAttachment(holder, world, () -> pos);
        }
    }

    @Override
    public ElementHolder holder() {
        return this.holder;
    }

    @Override
    public void destroy() {
        if (this.removed) return;
        this.removed = true;

        ((HolderAttachmentHolder) chunk).polymerVE$removeHolder(this);
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
    }

    @Override
    public void tick() {
        if (this.removed) return;
        if (this.autoTick) {
            this.holder().tick();
        }
    }

    @Override
    public boolean shouldTick() {
        return this.autoTick;
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerGamePacketListenerImpl> currentlyTracking) {
        if (this.removed || !((WorldChunkAccessor) chunk).isLoaded()) return;

        List<ServerGamePacketListenerImpl> watching = new ArrayList<>();

        for (ServerPlayer x : getPlayersWatchingChunk(chunk)) {
            ServerGamePacketListenerImpl networkHandler = x.connection;
            watching.add(networkHandler);
        }

        for (var player : currentlyTracking) {
            if (!watching.contains(player)) {
                this.holder.stopWatching(player);
            }
        }

        for (var x : watching) {
            this.holder.startWatching(x.getPlayer().connection);
        }
    }

    private static List<ServerPlayer> getPlayersWatchingChunk(LevelChunk chunk) {
        if (CompatStatus.IMMERSIVE_PORTALS) {
            return ImmersivePortalsUtils.getPlayerTracking(chunk);
        } else {
            return ((ServerChunkCache) chunk.getLevel().getChunkSource()).chunkMap.getPlayers(chunk.getPos(), false);
        }
    }

    @Override
    public void updateTracking(ServerGamePacketListenerImpl tracking) {
        if (this.removed) return;
        if (tracking.player.isDeadOrDying() || !VirtualEntityUtils.isPlayerTracking(tracking.getPlayer(), this.chunk)) {
            this.stopWatching(tracking);
        }
    }

    @Override
    public Vec3 getPos() {
        return this.pos;
    }

    @Override
    public ServerLevel getWorld() {
        return (ServerLevel) this.chunk.getLevel();
    }

    public LevelChunk getChunk() {
        return this.chunk;
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }
}
