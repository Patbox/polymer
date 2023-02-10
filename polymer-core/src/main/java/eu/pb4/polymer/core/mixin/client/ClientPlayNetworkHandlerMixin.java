package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.api.client.ClientPolymerBlock;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow public abstract ClientWorld getWorld();

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    private void polymer$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        InternalClientRegistry.syncRequestsPostGameJoin = 0;
    }

    @Inject(method = "onBlockUpdate", at = @At("TAIL"))
    private void polymer$removeOldBlock(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        // This should be overriden by next polymer packet anyway
        // Thanks to it there is no need to send vanilla updates!
        InternalClientRegistry.setBlockAt(packet.getPos(), ClientPolymerBlock.NONE_STATE);
    }

    @Inject(method = "method_34007", at = @At("TAIL"))
    private void polymer$removeOldBlock2(int i, BlockPos pos, BlockState state, CallbackInfo ci) {
        InternalClientRegistry.setBlockAt(pos, ClientPolymerBlock.NONE_STATE);
    }
}
