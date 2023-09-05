package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class ClientCommonNetworkHandlerMixin {
    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (ClientPacketRegistry.handle((ClientCommonNetworkHandler) (Object) this, packet.payload())) {
            ci.cancel();
        }
    }
}
