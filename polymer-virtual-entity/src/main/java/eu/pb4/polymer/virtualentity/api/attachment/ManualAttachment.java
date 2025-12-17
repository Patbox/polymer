package eu.pb4.polymer.virtualentity.api.attachment;

import eu.pb4.polymer.virtualentity.api.ElementHolder;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public final class ManualAttachment implements HolderAttachment {
    private final ElementHolder holder;
    private final ServerLevel world;
    private final Supplier<Vec3> posSupplier;

    public ManualAttachment(ElementHolder holder, ServerLevel world, Supplier<Vec3> posSupplier) {
        this.holder = holder;
        this.world = world;
        this.posSupplier = posSupplier;
        holder.setAttachment(this);
    }

    @Override
    public void destroy() {
        if (this.holder.getAttachment() == this) {
            this.holder.setAttachment(null);
        }
    }

    @Override
    public Vec3 getPos() {
        return this.posSupplier.get();
    }

    @Override
    public ServerLevel getWorld() {
        return this.world;
    }

    @Override
    public void updateCurrentlyTracking(Collection<ServerGamePacketListenerImpl> currentlyTracking) {
    }

    @Override
    public void updateTracking(ServerGamePacketListenerImpl tracking) {
    }

    @Override
    public ElementHolder holder() {
        return holder;
    }

    public ServerLevel world() {
        return world;
    }

    public Supplier<Vec3> posSupplier() {
        return posSupplier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ManualAttachment) obj;
        return Objects.equals(this.holder, that.holder) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.posSupplier, that.posSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, world, posSupplier);
    }

    @Override
    public String toString() {
        return "ManualAttachment[" +
                "holder=" + holder + ", " +
                "world=" + world + ", " +
                "posSupplier=" + posSupplier + ']';
    }

}
