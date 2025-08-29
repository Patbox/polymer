package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerChunkLoadingManager.class)
public abstract class ServerChunkLoadingManagerMixin {

    @Inject(method = "handlePlayerAddedOrRemoved", at = @At("TAIL"))
    private void polymerVE$clearHolograms(ServerPlayerEntity player, boolean added, CallbackInfo ci) {
        if (!added) {
            var holders = ((HolderHolder) player.networkHandler).polymer$getHolders();
            if (!holders.isEmpty()) {
                var arr = holders.toArray(HolderHolder.ELEMENT_HOLDERS);
                for (int i = 0; i < arr.length; i++) {
                    var holder = arr[i];
                    if (holder.getAttachment() != null) {
                        holder.getAttachment().updateTracking(player.networkHandler);
                    }
                }
            }
        }
    }
}
