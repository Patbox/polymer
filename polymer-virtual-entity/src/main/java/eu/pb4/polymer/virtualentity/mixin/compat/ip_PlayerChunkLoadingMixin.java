package eu.pb4.polymer.virtualentity.mixin.compat;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.chunk_loading.PlayerChunkLoading;
import qouteall.imm_ptl.core.network.PacketRedirection;

@Pseudo
@Mixin(value = PlayerChunkLoading.class)
public class ip_PlayerChunkLoadingMixin {

    @Inject(method = "sendChunkPacket", at = @At("TAIL"), require = 0)
    private static void polymerVE$addToPlayerPlayer(ServerPlayNetworkHandler serverGamePacketListenerImpl, ServerWorld serverLevel, WorldChunk levelChunk, CallbackInfo ci) {
        if (!serverGamePacketListenerImpl.player.isDead()) {
            PacketRedirection.withForceRedirect(serverLevel, () -> {
                for (var hologram : ((HolderAttachmentHolder) levelChunk).polymerVE$getHolders()) {
                    hologram.startWatching(serverGamePacketListenerImpl);
                }
            });
        }
    }
}
