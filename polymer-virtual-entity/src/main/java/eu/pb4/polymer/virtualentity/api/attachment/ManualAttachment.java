package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Collection;
import java.util.function.Supplier;

public record ManualAttachment(ElementHolder holder, ServerWorld world, Supplier<Vec3d> posSupplier) implements HolderAttachment  {

    public ManualAttachment {
        holder.setAttachment(this);
    }

    @Override
    public void destroy() {
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
    }

    @Override
    public Vec3d getPos() {
        return this.posSupplier.get();
    }

    @Override
    public ServerWorld getWorld() {
        return this.world;
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerPlayNetworkHandler> currentlyTracking) {}

    @Override
    public void updateTracking(ServerPlayNetworkHandler tracking) {}
}
