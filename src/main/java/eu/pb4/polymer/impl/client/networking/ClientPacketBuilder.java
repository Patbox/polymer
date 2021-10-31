package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.impl.networking.PolymerPacketIds;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientPacketBuilder {
    public static PacketByteBuf buf() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void sendVersion(ClientPlayNetworkHandler handler) {
        var buf = buf();

        buf.writeShort(0);
        buf.writeString("0.2.0");

        handler.sendPacket(new CustomPayloadC2SPacket(PolymerPacketIds.VERSION_ID, buf));
    }
}
