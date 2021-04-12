package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.interfaces.PlayerContextInterface;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Future;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerContextMixin {
    @Shadow
    public ServerPlayerEntity player;


    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"))
    public void setPlayerInPacket(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof PlayerContextInterface) {
            ((PlayerContextInterface) packet).setPolymerPlayer(this.player);
        }
    }
}
