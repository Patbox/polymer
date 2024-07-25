package eu.pb4.polymer.core.mixin.block;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.world.chunk.WorldChunk;
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


    @WrapWithCondition(method = "unload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"), require = 0)
    private boolean skipChunkClearing(ServerPlayNetworkHandler instance, Packet packet) {
        return PolymerImplUtils.IS_RELOADING_WORLD.get() == null;
    }
}
