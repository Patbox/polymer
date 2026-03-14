package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HandshakePayload(String version, Map<Identifier, int[]> packetVersions) implements CustomPacketPayload {
    public static final Type<HandshakePayload> ID = PolymerNetworking.id("polymer", "handshake");
    public static StreamCodec<ContextByteBuf, HandshakePayload> CODEC = StreamCodec.ofMember(HandshakePayload::write, HandshakePayload::read);

    public void write(ContextByteBuf buf) {
        buf.writeUtf(this.version);
        buf.writeMap(packetVersions, FriendlyByteBuf::writeIdentifier, FriendlyByteBuf::writeVarIntArray);
    }

    public static HandshakePayload read(ContextByteBuf buf) {
        return new HandshakePayload(buf.readUtf(), buf.readMap(FriendlyByteBuf::readIdentifier, FriendlyByteBuf::readVarIntArray));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
