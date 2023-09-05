package eu.pb4.polymer.networking.api;

import eu.pb4.polymer.networking.impl.CustomPayloadS2CExt;
import io.netty.buffer.Unpooled;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface ServerPacketWriter {
    void write(ServerPlayNetworkHandler player, PacketByteBuf buf, Identifier packetId, int version);

    default Packet<ClientCommonPacketListener> toPacket(Identifier identifier) {
        //var base = new CustomPayloadS2CPacket(identifier, new PacketByteBuf(Unpooled.EMPTY_BUFFER));
        //((CustomPayloadS2CExt) base).polymerNet$setWriter(this);
        return new CommonPingS2CPacket(0);
    }
}
