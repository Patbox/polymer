package eu.pb4.polymer.core.mixin.block;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.block.BlockMapper;
import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkDataSender.class)
public class ChunkDataSenderMixin {
    @Inject(method = "sendChunkData", at = @At("HEAD"), require = 0)
    private static void polymer$catchPlayer(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk, CallbackInfo ci) {
        CommonImplUtils.setPlayer(handler.player);
    }

    @Inject(method = "sendChunkData", at = @At("TAIL"), require = 0)
    private static void polymer$clearPlayer(ServerPlayNetworkHandler handler, ServerWorld world, WorldChunk chunk, CallbackInfo ci) {
        CommonImplUtils.setPlayer(null);
    }
}
