package eu.pb4.polymer.virtualentity.mixin.block;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;


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

    @Inject(method = "method_60440", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;setLoadedToWorld(Z)V", shift = At.Shift.AFTER))
    private void onChunkUnload(ChunkHolder chunkHolder, long l, CallbackInfo ci, @Local WorldChunk chunk) {
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
