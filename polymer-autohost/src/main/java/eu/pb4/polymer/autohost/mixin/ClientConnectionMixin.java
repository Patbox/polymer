package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.autohost.impl.ClientConnectionExt;
import eu.pb4.polymer.autohost.impl.netty.ProtocolSwitcher;
import eu.pb4.polymer.autohost.impl.providers.NettyProvider;
import io.netty.channel.ChannelPipeline;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.handler.PacketSizeLogger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = 10000)
public class ClientConnectionMixin implements ClientConnectionExt {
    @Unique
    private String connAddress = "";
    @Unique
    private int connPort = -1;

    @Override
    public void polymerAutoHost$setAddress(String address, int port) {
        this.connAddress = address;
        this.connPort = port;
    }

    @Override
    public String polymerAutoHost$getAddress() {
        return this.connAddress;
    }

    @Override
    public int polymerAutoHost$getPort() {
        return this.connPort;
    }

    @Inject(method = "addHandlers", at = @At("TAIL"))
    private static void addHttpHandlers(ChannelPipeline pipeline, NetworkSide side, boolean bl, PacketSizeLogger packetSizeLogger, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND && ResourcePackDataProvider.getActive() instanceof NettyProvider) {
            pipeline.addFirst(ProtocolSwitcher.ID, new ProtocolSwitcher());
        }
    }
}
