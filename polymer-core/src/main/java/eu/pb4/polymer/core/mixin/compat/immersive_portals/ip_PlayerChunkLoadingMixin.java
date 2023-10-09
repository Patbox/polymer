package eu.pb4.polymer.core.mixin.compat.immersive_portals;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.PlayerChunkLoading;


@Pseudo
@Mixin(value = PlayerChunkLoading.class)
public class ip_PlayerChunkLoadingMixin {
    @Inject(method = "sendChunkPacket", at = @At("HEAD"), require = 0)
    private static void polymer_setPlayerNow(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel, WorldChunk levelChunk, CallbackInfo ci) {
        CommonImplUtils.setPlayer(serverGamePacketListenerImpl.getPlayer());
    }

    @Inject(method = "sendChunkPacket", at = @At("TAIL"), require = 0)
    private static void polymer_resetPlayer(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel, WorldChunk levelChunk, CallbackInfo ci) {
        CommonImplUtils.setPlayer(null);
    }
}
