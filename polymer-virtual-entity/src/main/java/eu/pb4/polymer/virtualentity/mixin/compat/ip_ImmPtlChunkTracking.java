package eu.pb4.polymer.virtualentity.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.api.attachment.ChunkAttachment;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;

import java.util.ArrayList;
import java.util.Map;

@Pseudo
@Mixin(ImmPtlChunkTracking.class)
public class ip_ImmPtlChunkTracking {
    @Inject(method = "lambda$purge$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private static void polymerVE$chunkUnload(Map.Entry e, CallbackInfoReturnable<Boolean> cir, @Local ServerPlayerEntity player, @Local ImmPtlChunkTracking.PlayerWatchRecord record) {
        for (var holder : new ArrayList<>(((HolderHolder) player.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() instanceof ChunkAttachment chunkAttachment
                    && chunkAttachment.getWorld().getRegistryKey().equals(record.dimension)
                    && holder.getChunkPos().toLong() == record.chunkPos) {
                holder.getAttachment().stopWatching(player.networkHandler);
            }
        }
    }

    @Inject(method = "forceRemovePlayer", at = @At("TAIL"), require = 0)
    private static void polymerVE$chunkUnload2(ServerPlayerEntity oldPlayer, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) oldPlayer.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() instanceof ChunkAttachment chunkAttachment) {
                holder.getAttachment().stopWatching(oldPlayer.networkHandler);
            }
        }
    }
}
