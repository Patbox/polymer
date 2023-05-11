package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.mixin.accessors.EntityTrackerAccessor;
import eu.pb4.polymer.virtualentity.mixin.accessors.ThreadedAnvilChunkStorageAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;

@SuppressWarnings("ClassCanBeRecord")
public class EntityAttachment implements HolderAttachment {
    private final Entity entity;
    private final ElementHolder holder;
    private final boolean autoTick;

    public EntityAttachment(ElementHolder holder, Entity entity, boolean autoTick) {
        this.entity = entity;
        this.holder = holder;
        ((HolderAttachmentHolder) entity).polymerVE$addHolder(this);
        this.holder.setAttachment(this);
        this.autoTick = autoTick;
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
        ((HolderAttachmentHolder) entity).polymerVE$removeHolder(this);
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
    }

    @Override
    public void tick() {
        if (this.autoTick) {
            this.holder.tick();
        }
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerPlayNetworkHandler> currentlyTracking) {
        var entry = getTrackerEntry();

        if (entry == null) {
            for (var x : currentlyTracking) {
                this.holder.stopWatching(x);
            }
            return;
        }

        var watching = ((EntityTrackerAccessor) entry).getListeners();

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
    public boolean canUpdatePosition() {
        return !this.entity.isRemoved() && this.entity.getWorld().getEntityById(this.entity.getId()) == this.entity;
    }

    @Override
    public void updateTracking(ServerPlayNetworkHandler tracking) {
        // left that to impl logic
    }

    private ThreadedAnvilChunkStorage.EntityTracker getTrackerEntry() {
        return ((ThreadedAnvilChunkStorageAccessor) ((ServerWorld) this.entity.getWorld()).getChunkManager().threadedAnvilChunkStorage).getEntityTrackers().get(this.entity.getId());
    }

    @Override
    public Vec3d getPos() {
        return this.entity.getPos();
    }

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) this.entity.getWorld();
    }


    @Override
    public boolean shouldTick() {
        return this.autoTick;
    }
}
