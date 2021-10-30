package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.networking.ClientPacketBuilder;
import eu.pb4.polymer.impl.client.networking.ClientPacketHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onCustomPayload", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
    private void polymer_catchPackets(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.getChannel().getNamespace().equals(PolymerUtils.ID)) {
            ClientPacketHandler.handle((ClientPlayNetworkHandler) (Object) this, packet.getChannel(), packet.getData());
            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void polymer_sendVersion(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientPacketBuilder.sendVersion((ClientPlayNetworkHandler) (Object) this);
    }
}
