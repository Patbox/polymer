package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ChunkMap.class)
public abstract class ServerChunkLoadingManagerMixin {

    @Inject(method = "updatePlayerStatus", at = @At("TAIL"))
    private void polymerVE$clearHolograms(ServerPlayer player, boolean added, CallbackInfo ci) {
        if (!added) {
            var holders = ((HolderHolder) player.connection).polymer$getHolders();
            if (!holders.isEmpty()) {
                var arr = holders.toArray(HolderHolder.ELEMENT_HOLDERS);
                for (int i = 0; i < arr.length; i++) {
                    var holder = arr[i];
                    if (holder.getAttachment() != null) {
                        holder.getAttachment().updateTracking(player.connection);
                    }
                }
            }
        }
    }
}
