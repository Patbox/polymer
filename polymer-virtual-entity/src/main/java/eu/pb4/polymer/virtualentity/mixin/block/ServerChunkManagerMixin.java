package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Inject(method = "tickChunks(Lnet/minecraft/util/profiler/Profiler;JLjava/util/List;)V", at = @At("TAIL"))
    private void tickElementHoldersEvenIfBlocksDont(Profiler profiler, long l, List<WorldChunk> list, CallbackInfo ci) {
        for (var chunk : list) {
            var holo = ((HolderAttachmentHolder) chunk).polymerVE$getHolders();

            if (!holo.isEmpty()) {
                var arr = holo.toArray(HolderHolder.HOLDER_ATTACHMENTS);
                for (int i = 0; i < arr.length; i++) {
                    arr[i].tick();
                }
            }
        }
    }
}
