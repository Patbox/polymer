package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.ExtClientConnection;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ExtClientConnection {
    @Shadow private int packetsReceivedCounter;
    private Consumer<CustomPayloadC2SPacket> polymer_packetConsumer;

    @Inject(method = "setPacketListener", at = @At("HEAD"))
    private void polymer_removeConsumer(PacketListener listener, CallbackInfo ci) {
        this.polymer_packetConsumer = null;
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void polymer_handlePacket(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (this.polymer_packetConsumer != null) {
            if (packet instanceof CustomPayloadC2SPacket c) {
                this.polymer_packetConsumer.accept(c);
            }
            ci.cancel();
            this.packetsReceivedCounter++;
        }
    }

    @Override
    public void polymer_ignorePacketsUntilChange(Consumer<CustomPayloadC2SPacket> consumer) {
        this.polymer_packetConsumer = consumer;
    }
}
