package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.impl.ExtClientConnection;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ExtClientConnection {
    @Shadow private int packetsReceivedCounter;
    private Consumer<CustomPayloadC2SPacket> polymerNet$packetConsumer;
    private boolean polymerNet$packetActive;

    @Inject(method = "setPacketListener", at = @At("HEAD"))
    private void polymerNet$removeConsumer(PacketListener listener, CallbackInfo ci) {
        this.polymerNet$packetActive = listener instanceof ServerLoginNetworkHandler;
        if (listener instanceof ServerPlayNetworkHandler) {
            this.polymerNet$packetConsumer = null;
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void polymerNet$handlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (this.polymerNet$packetActive && this.polymerNet$packetConsumer != null) {
            if (packet instanceof CustomPayloadC2SPacket c) {
                this.polymerNet$packetConsumer.accept(c);
            }
            ci.cancel();
            this.packetsReceivedCounter++;
        }
    }

    @Override
    public void polymerNet$ignorePacketsUntilChange(Consumer<CustomPayloadC2SPacket> consumer) {
        this.polymerNet$packetConsumer = consumer;
    }
}
