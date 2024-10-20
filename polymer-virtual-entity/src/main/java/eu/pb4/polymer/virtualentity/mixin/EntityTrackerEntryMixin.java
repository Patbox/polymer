package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils;
import eu.pb4.polymer.virtualentity.impl.EntityExt;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity entity;

    @Shadow private List<Entity> lastPassengers;

    @Shadow @Final private Consumer<Packet<?>> receiver;

    @Inject(method = "startTracking", at = @At("TAIL"))
    private void polymerVE$startTracking(ServerPlayerEntity player, CallbackInfo ci) {
        boolean hasPassangers = false;
        var a = ((HolderAttachmentHolder) this.entity).polymerVE$getHolders();
        if (!a.isEmpty()) {
            for (var x : a) {
                x.startWatching(player);
                hasPassangers |= !x.holder().getAttachedPassengerEntityIds().isEmpty();
            }
        }

        if (hasPassangers || !((EntityExt) this.entity).polymerVE$getVirtualRidden().isEmpty()) {
            player.networkHandler.sendPacket(new EntityPassengersSetS2CPacket(this.entity));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void polymerVE$tick(CallbackInfo ci) {
        var a = ((HolderAttachmentHolder) this.entity).polymerVE$getHolders();
        if (!a.isEmpty()) {
            var arr = a.toArray(HolderHolder.HOLDER_ATTACHMENTS);
            for (int i = 0; i < arr.length; i++) {
                arr[i].tick();
            }
        }

        if (((EntityExt) this.entity).polymerVE$getAndClearVirtualRiddenDirty() && this.entity.getPassengerList().equals(this.lastPassengers)) {
            this.receiver.accept(new EntityPassengersSetS2CPacket(this.entity));
        }
    }

    @Inject(method = "stopTracking", at = @At("TAIL"))
    private void polymerVE$stopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        for (var x : ((HolderAttachmentHolder) this.entity).polymerVE$getHolders()) {
            x.stopWatching(player);
        }
    }
}
