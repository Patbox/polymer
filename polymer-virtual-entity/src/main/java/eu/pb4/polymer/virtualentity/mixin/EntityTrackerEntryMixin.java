package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow @Final private Entity entity;

    @Inject(method = "startTracking", at = @At("TAIL"))
    private void polymerVE$startTracking(ServerPlayerEntity player, CallbackInfo ci) {
        for (var x : ((HolderAttachmentHolder) this.entity).polymer$getHolders()) {
            x.startWatching(player);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymerVE$tick(CallbackInfo ci) {
        for (var x : ((HolderAttachmentHolder) this.entity).polymer$getHolders()) {
            x.tick();
        }
    }

    @Inject(method = "stopTracking", at = @At("TAIL"))
    private void polymerVE$stopTracking(ServerPlayerEntity player, CallbackInfo ci) {
        for (var x : ((HolderAttachmentHolder) this.entity).polymer$getHolders()) {
            x.stopWatching(player);
        }
    }
}
