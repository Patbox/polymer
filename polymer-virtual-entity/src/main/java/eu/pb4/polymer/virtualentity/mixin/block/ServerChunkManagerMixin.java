package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Inject(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void tickElementHoldersEvenIfBlocksDont(CallbackInfo ci, @Local List<ServerChunkManager.ChunkWithHolder> chunkList) {
        chunkList.forEach(this::tickHolders);
    }

    @Unique
    private void tickHolders(ServerChunkManager.ChunkWithHolder chunkWithHolder) {
        var holo = ((HolderAttachmentHolder) chunkWithHolder.chunk()).polymerVE$getHolders();

        if (!holo.isEmpty()) {
            var arr = holo.toArray(HolderHolder.HOLDER_ATTACHMENTS);
            for (int i = 0; i < arr.length; i++) {
                arr[i].tick();
            }
        }
    }
}
