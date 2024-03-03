package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.ClientConnectionExt;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerHandshakeNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakeNetworkHandler.class)
public class ServerHandshakeNetworkHandlerMixin {
    @Shadow @Final private ClientConnection connection;

    @Inject(method = "login", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;transitionInbound(Lnet/minecraft/network/NetworkState;Lnet/minecraft/network/listener/PacketListener;)V"))
    private void storeConnectionData(HandshakeC2SPacket packet, boolean transfer, CallbackInfo ci) {
        ((ClientConnectionExt) this.connection).polymerAutoHost$setAddress(packet.address(), packet.port());
    }
}
