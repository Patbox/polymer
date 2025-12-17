package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerWorldMixin {

    @Inject(method = "tickChunk", at = @At("TAIL"))
    private void tickHolders(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        var holo = ((HolderAttachmentHolder) chunk).polymerVE$getHolders();

        if (!holo.isEmpty()) {
            var arr = holo.toArray(HolderHolder.HOLDER_ATTACHMENTS);
            for (int i = 0; i < arr.length; i++) {
                arr[i].tick();
            }
        }
    }
}
