package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public record HandshakePayload(String version, Map<Identifier, int[]> packetVersions) implements CustomPayload {
    public static final Id<HandshakePayload> ID = PolymerNetworking.id("polymer", "handshake");
    public static PacketCodec<ContextByteBuf, HandshakePayload> CODEC = PacketCodec.of(HandshakePayload::write, HandshakePayload::read);

    public void write(ContextByteBuf buf) {
        buf.writeString(this.version);
        buf.writeMap(packetVersions, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntArray);
    }

    public static HandshakePayload read(ContextByteBuf buf) {
        return new HandshakePayload(buf.readString(), buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readIntArray));
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
