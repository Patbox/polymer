package eu.pb4.polymer.autohost.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.ConnectionExt;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplMixin {
    @Shadow @Final private Connection connection;

    @ModifyExpressionValue(method = "handleIntention", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;repliesToStatus()Z"))
    private boolean delayOnlineStatusForAutohost(boolean original) {
        if (AutoHost.config.delayPlayerListMotd && AutoHost.config.enabled && !AutoHost.provider.isReady(this.connection.getPacketContext())) {
            return false;
        }
        return original;
    }

    @Inject(method = "beginLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"))
    private void storeConnectionData(ClientIntentionPacket packet, boolean transfer, CallbackInfo ci) {
        ((ConnectionExt) this.connection).polymerAutoHost$setAddress(packet.hostName(), packet.port());
    }
}
