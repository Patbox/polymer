package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public record HandshakePayload(String version, Map<Identifier, int[]> packetVersions) implements CustomPacketPayload {
    public static final Type<HandshakePayload> ID = PolymerNetworking.id("polymer", "handshake");
    public static StreamCodec<ContextByteBuf, HandshakePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.cast(),
            HandshakePayload::version,
            ByteBufCodecs.map((IntFunction<Map<Identifier, int[]>>) HashMap::new, Identifier.STREAM_CODEC, new StreamCodec<ContextByteBuf, int[]>() {

                @Override
                public void encode(ContextByteBuf output, int[] value) {
                    output.writeVarIntArray(value);
                }

                @Override
                public int[] decode(ContextByteBuf input) {
                    return input.readVarIntArray();
                }
            }), HandshakePayload::packetVersions,
            HandshakePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
