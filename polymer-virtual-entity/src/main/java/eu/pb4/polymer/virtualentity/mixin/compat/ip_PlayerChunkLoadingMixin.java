package eu.pb4.polymer.virtualentity.mixin.compat;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
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
    private static void polymerVE$addToPlayerPlayer(ServerGamePacketListenerImpl serverGamePacketListenerImpl, ServerLevel serverLevel, LevelChunk levelChunk, CallbackInfo ci) {
        if (!serverGamePacketListenerImpl.player.isDeadOrDying()) {
            PacketRedirection.withForceRedirect(serverLevel, () -> {
                for (var hologram : ((HolderAttachmentHolder) levelChunk).polymerVE$getHolders()) {
                    hologram.startWatching(serverGamePacketListenerImpl);
                }
            });
        }
    }
}
