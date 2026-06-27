package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.mixin.accessors.TrackedEntityAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ChunkMapAccessor;
import java.util.Collection;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("ClassCanBeRecord")
public class EntityAttachment implements HolderAttachment {
    protected final Entity entity;
    private final ElementHolder holder;
    private final boolean autoTick;

    private boolean removed = false;

    public EntityAttachment(ElementHolder holder, Entity entity, boolean autoTick) {
        this.entity = entity;
        this.holder = holder;
        this.autoTick = autoTick;
        if (this.getClass() == EntityAttachment.class) {
            this.attach();
        }
    }

    protected void attach() {
        this.holder.setAttachment(this);
        ((HolderAttachmentHolder) entity).polymerVE$addHolder(this);
    }

    public static EntityAttachment of(ElementHolder holder, Entity entity) {
        return new EntityAttachment(holder, entity, false);
    }

    public static EntityAttachment ofTicking(ElementHolder holder, Entity entity) {
        return new EntityAttachment(holder, entity, true);
    }

    @Override
    public ElementHolder holder() {
        return this.holder;
    }

    @Override
    public void destroy() {
        if (this.removed) return;
        this.removed = true;

        ((HolderAttachmentHolder) entity).polymerVE$removeHolder(this);
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
    }

    @Override
    public void tick() {
        if (this.removed) return;

        if (this.autoTick) {
            this.holder.tick();
        }
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerGamePacketListenerImpl> currentlyTracking) {
        if (this.removed) return;

        if (this.holder.getAttachment() != this) {
            return;
        }

        var entry = getTrackerEntry();

        if (entry == null) {
            for (var x : currentlyTracking) {
                this.holder.stopWatching(x);
            }
            return;
        }

        var watching = ((TrackedEntityAccessor) entry).getSeenBy();

        for (var player : currentlyTracking) {
            if (!watching.contains(player)) {
                this.holder.stopWatching(player);
            }
        }

        for (var x : watching) {
            this.holder.startWatching(x.getPlayer().connection);
        }
    }

    @Override
    public boolean canUpdatePosition() {
        try {
            return !this.removed && !this.entity.isRemoved() && this.entity.level().getEntity(this.entity.getId()) == this.entity;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public void updateTracking(ServerGamePacketListenerImpl tracking) {
        // left that to impl logic
    }

    @Nullable
    private ChunkMap.TrackedEntity getTrackerEntry() {
        try {
            return ((ChunkMapAccessor) ((ServerLevel) this.entity.level()).getChunkSource().chunkMap).getEntityTrackers().get(this.entity.getId());
        } catch (Throwable e) {
            return null;
        }
    }

    @Override
    public Vec3 getPos() {
        return this.entity.position();
    }

    @Override
    public ServerLevel getWorld() {
        return (ServerLevel) this.entity.level();
    }


    @Override
    public boolean shouldTick() {
        return this.autoTick;
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }
}
