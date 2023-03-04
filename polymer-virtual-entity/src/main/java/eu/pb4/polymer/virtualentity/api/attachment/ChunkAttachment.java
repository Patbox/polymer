package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ChunkAttachment implements HolderAttachment {
    private final ElementHolder holder;
    private final WorldChunk chunk;
    private final Vec3d pos;
    private final boolean autoTick;

    public ChunkAttachment(ElementHolder holder, WorldChunk chunk, Vec3d position, boolean autoTick) {
        this.chunk = chunk;
        this.pos = position;
        this.holder = holder;
        ((HolderAttachmentHolder) chunk).polymer$addHolder(this);
        this.holder.setAttachment(this);
        this.autoTick = autoTick;
    }

    @Override
    public ElementHolder holder() {
        return this.holder;
    }

    @Override
    public void destroy() {
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
        ((HolderAttachmentHolder) chunk).polymer$removeHolder(this);
    }

    @Override
    public void tick() {
        if (this.autoTick) {
            this.holder().tick();
        }
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerPlayNetworkHandler> currentlyTracking) {
        List<ServerPlayNetworkHandler> watching = new ArrayList<>();
        for (ServerPlayerEntity x : ((ServerChunkManager) this.chunk.getWorld().getChunkManager()).threadedAnvilChunkStorage.getPlayersWatchingChunk(this.chunk.getPos(), false)) {
            ServerPlayNetworkHandler networkHandler = x.networkHandler;
            watching.add(networkHandler);
        }

        for (var player : currentlyTracking) {
            if (!watching.contains(player)) {
                this.holder.stopWatching(player);
            }
        }

        for (var x : watching) {
            this.holder.startWatching(x.getPlayer().networkHandler);
        }
    }

    @Override
    public void updateTracking(ServerPlayNetworkHandler tracking) {
        var tacs = tracking.player.getWorld().getChunkManager().threadedAnvilChunkStorage;
        var section = tracking.getPlayer().getWatchedSection();
        if (!tacs.isWithinDistance(this.chunk.getPos().x, this.chunk.getPos().z, section.getX(), section.getMaxZ(), ((ThreadedAnvilChunkStorageAccessor) tacs).getWatchDistance())) {
            this.stopWatching(tracking);
        }
    }

    @Override
    public Vec3d getPos() {
        return this.pos;
    }

}
