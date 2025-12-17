package eu.pb4.polymer.virtualentity.mixin;

import eu.pb4.polymer.virtualentity.impl.HolderAttachmentHolder;
import eu.pb4.polymer.virtualentity.impl.HolderHolder;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(PlayerChunkSender.class)
public class ChunkDataSenderMixin {
    @Inject(method = "sendChunk", at = @At("TAIL"), require = 0)
    private static void polymerVE$addToHolograms(ServerGamePacketListenerImpl handler, ServerLevel world, LevelChunk chunk, CallbackInfo ci) {
        for (var hologram : ((HolderAttachmentHolder) chunk).polymerVE$getHolders()) {
            hologram.startWatching(handler);
        }
    }

    @Inject(method = "dropChunk", at = @At("HEAD"), require = 0)
    private void polymerVE$chunkUnload(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        for (var holder : new ArrayList<>(((HolderHolder) player.connection).polymer$getHolders())) {
            var att = holder.getAttachment();
            if (att != null && holder.getChunkPos().equals(pos)) {
                att.updateTracking(player.connection);
            }
        }
    }
}
