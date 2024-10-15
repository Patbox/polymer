package eu.pb4.polymer.virtualentity.mixin.compat;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;

import java.util.ArrayList;
import java.util.Map;

@Pseudo
@Mixin(ImmPtlChunkTracking.class)
public class ip_ImmPtlChunkTracking {
    @Inject(method = "lambda$purge$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private static void polymerVE$chunkUnload(Map.Entry e, CallbackInfoReturnable<Boolean> cir, @Local ServerPlayerEntity player, @Local ImmPtlChunkTracking.PlayerWatchRecord record) {
        for (var holder : new ArrayList<>(((HolderHolder) player.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null && holder.getChunkPos().toLong() == record.chunkPos) {
                holder.getAttachment().updateTracking(player.networkHandler);
            }
        }
    }

    @Inject(method = "lambda$forceRemovePlayer$16", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/network/PacketRedirection;sendRedirectedMessage(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private static void polymerVE$chunkUnload2(ServerPlayerEntity player, RegistryKey dim, Long2ObjectMap.Entry e, CallbackInfoReturnable<Boolean> cir, @Local ImmPtlChunkTracking.PlayerWatchRecord rec) {
        for (var holder : new ArrayList<>(((HolderHolder) player.networkHandler).polymer$getHolders())) {
            if (holder.getAttachment() != null && holder.getChunkPos().toLong() == rec.chunkPos) {
                holder.getAttachment().updateTracking(player.networkHandler);
            }
        }
    }
}
