package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.impl.PacketListenerImplExtension;
import eu.pb4.polymer.networking.impl.ServerPacketRegistry;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements PacketListenerImplExtension {
    public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (ServerPacketRegistry.handle(this.server, (ServerCommonPacketListenerImpl) (Object) this, packet.payload())) {
            this.polymerNet$savePacketTime(packet.payload().type().id());
            ci.cancel();
        }
    }
}
