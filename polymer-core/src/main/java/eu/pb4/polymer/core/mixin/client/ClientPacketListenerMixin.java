package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Shadow public abstract ClientLevel getLevel();

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift = At.Shift.AFTER))
    private void polymer$onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        InternalClientRegistry.syncRequestsPostGameJoin = 0;
    }

    @Inject(method = "handleBlockUpdate", at = @At("TAIL"))
    private void polymer$removeOldBlock(ClientboundBlockUpdatePacket packet, CallbackInfo ci) {
        // This should be overriden by next polymer packet anyway
        // Thanks to it there is no need to send vanilla updates!
        InternalClientRegistry.setBlockAt(packet.getPos(), ClientPolymerBlock.NONE_STATE);
    }

    @Inject(method = "lambda$handleChunkBlocksUpdate$0", at = @At("TAIL"))
    private void polymer$removeOldBlock2(BlockPos pos, BlockState state, CallbackInfo ci) {
        InternalClientRegistry.setBlockAt(pos, ClientPolymerBlock.NONE_STATE);
    }
}
