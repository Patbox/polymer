package eu.pb4.polymer.virtualentity.mixin.compat;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import qouteall.imm_ptl.core.chunk_loading.ChunkDataSyncManager;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.ducks.IEThreadedAnvilChunkStorage;
import qouteall.imm_ptl.core.network.PacketRedirection;

import java.util.ArrayList;
import java.util.function.Supplier;

@Pseudo
@Mixin(value = ChunkDataSyncManager.class)
public class ip_ChunkDataSyncManagerMixin {
    @Inject(method = "lambda$onChunkProvidedDeferred$1", at = @At("TAIL"), require = 0)
    private static void polymerVE$addToPlayerPlayer(Supplier chunkDataPacketRedirected, IEThreadedAnvilChunkStorage ieStorage, WorldChunk chunk, ServerPlayerEntity player, CallbackInfo ci) {
        if (!player.isDead()) {
            PacketRedirection.withForceRedirect(ieStorage.ip_getWorld(), () -> {
                for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
                    hologram.startWatching(player.networkHandler);
                }
            });
        }
    }

    @Inject(method = "sendChunkDataPacketNow", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.BEFORE), require = 0, locals = LocalCapture.CAPTURE_FAILHARD)
    private void polymerVE$addToSinglePlayer(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci, ChunkHolder chunkHolder) {
        if (!player.isDead()) {
            var chunk = chunkHolder.getWorldChunk();
            PacketRedirection.withForceRedirect(ieStorage.ip_getWorld(), () -> {
                for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
                    hologram.startWatching(player.networkHandler);
                }
            });
        }
    }

    @Inject(method = "onEndWatch", at = @At("RETURN"), require = 0)
    private void polymerVE$removeFromPlayer(ServerPlayerEntity player, DimensionalChunkPos chunkPos, CallbackInfo ci) {
        var actualPos = chunkPos.getChunkPos();
        for (var holder : new ArrayList<>(((HolderHolder) player.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null && holder.getAttachment().getWorld().getRegistryKey().equals(chunkPos.dimension) && holder.getChunkPos().equals(actualPos)) {
                PacketRedirection.withForceRedirect(holder.getAttachment().getWorld(), () -> {
                    holder.getAttachment().updateTracking(player.networkHandler);
                });
            }
        }
    }
}
