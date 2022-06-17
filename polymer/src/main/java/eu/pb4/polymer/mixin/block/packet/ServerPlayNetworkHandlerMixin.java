package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.impl.networking.BlockPacketUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", shift = At.Shift.AFTER))
    private void polymer_catchBlockUpdates(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo cb) {
        try {
            BlockPacketUtil.sendFromPacket(packet, (ServerPlayNetworkHandler) (Object) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
