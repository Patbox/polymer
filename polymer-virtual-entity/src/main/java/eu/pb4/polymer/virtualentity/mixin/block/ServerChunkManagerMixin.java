package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import eu.pb4.polymer.virtualentity.mixin.accessors.ServerChunkLoadingManagerAccessor;
import net.minecraft.server.world.ChunkLevelManager;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {
    @Shadow @Final private ChunkLevelManager levelManager;

    @Shadow @Final public ServerChunkLoadingManager chunkLoadingManager;

    @Inject(method = "tickChunks(Lnet/minecraft/util/profiler/Profiler;J)V", at = @At("TAIL"))
    private void tickElementHoldersEvenIfBlocksDont(Profiler profiler, long timeDelta, CallbackInfo ci) {
        for (var chunkHolder : ((ServerChunkLoadingManagerAccessor) this.chunkLoadingManager).getChunkHolders().values()) {
            var chunk = chunkHolder.getLevelType().isAfter(ChunkLevelType.FULL) ? chunkHolder.getWorldChunk() : null;

            if (chunk != null) {
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
}
