package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import qouteall.imm_ptl.core.chunk_loading.ChunkDataSyncManager;
import qouteall.imm_ptl.core.chunk_loading.DimensionalChunkPos;
import qouteall.imm_ptl.core.ducks.IEThreadedAnvilChunkStorage;
import qouteall.q_misc_util.Helper;

import java.util.function.Supplier;

@Mixin(value = ChunkDataSyncManager.class)
public class ip_ChunkDataSyncManagerMixin {
    @Inject(method = "sendChunkDataPacketNow", at = @At("HEAD"))
    private void polymer_setPlayerNow(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(player);
    }

    @Inject(method = "sendChunkDataPacketNow", at = @At("RETURN"))
    private void polymer_resetPlayerNow1(ServerPlayerEntity player, DimensionalChunkPos chunkPos, IEThreadedAnvilChunkStorage ieStorage, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(null);
    }

    @Redirect(method = "onChunkProvidedDeferred", at = @At(value = "INVOKE", target = "Lqouteall/q_misc_util/Helper;cached(Ljava/util/function/Supplier;)Ljava/util/function/Supplier;", ordinal = 0))
    private Supplier<?> polymer_setPlayerLambda(Supplier<?> supplier, WorldChunk chunk) {
        if (((PolymerBlockPosStorage) chunk).polymer_hasAny()) {
            return supplier;
        }

        return Helper.cached(supplier);
    }

    @Inject(method = "lambda$onChunkProvidedDeferred$1", at = @At("HEAD"))
    private static void polymer_setPlayer(Supplier chunkDataPacketRedirected, IEThreadedAnvilChunkStorage ieStorage, WorldChunk chunk, ServerPlayerEntity player, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(player);
    }

    @Inject(method = "lambda$onChunkProvidedDeferred$1", at = @At("TAIL"))
    private static void polymer_resetPlayer(Supplier chunkDataPacketRedirected, IEThreadedAnvilChunkStorage ieStorage, WorldChunk chunk, ServerPlayerEntity player, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(null);
    }
}
