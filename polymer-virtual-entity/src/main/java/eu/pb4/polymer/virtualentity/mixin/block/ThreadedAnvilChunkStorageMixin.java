package eu.pb4.polymer.virtualentity.mixin.block;

import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Inject(method = "sendChunkDataPackets", at = @At("TAIL"), require = 0)
    private void polymerVE$addToHolograms(ServerPlayerEntity player, MutableObject<ChunkDataS2CPacket> cachedDataPacket, WorldChunk chunk, CallbackInfo ci) {
        for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
            hologram.startWatching(player.networkHandler);
        }
    }

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

    @Inject(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoadedToWorld(Z)V", shift = At.Shift.AFTER))
    private void onChunkUnload(ChunkHolder chunkHolder, CompletableFuture completableFuture, long l, Chunk chunk, CallbackInfo ci) {
        if (chunk instanceof HolderAttachmentHolder x) {
            var holders = x.polymerVE$getHolders();
            if (!holders.isEmpty()) {
                var arr = holders.toArray(HolderHolder.HOLDER_ATTACHMENTS);
                for (int i = 0; i < arr.length; i++) {
                    var holder = arr[i];
                    if (holder != null) {
                        holder.destroy();
                    }
                }
            }
        }
    }
}
