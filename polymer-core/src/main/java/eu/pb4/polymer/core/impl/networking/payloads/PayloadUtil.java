package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.codec.PacketCodec;

public interface PayloadUtil {
    int PROTOCOL = SharedConstants.getProtocolVersion();

    @SuppressWarnings("unchecked")
    static <T> PacketCodec<ContextByteBuf, T> protocolSecured(PacketCodec<ContextByteBuf, T> codec) {
        var c = (PacketCodec<ContextByteBuf, Object>) codec;
        return (PacketCodec<ContextByteBuf, T>) (Object) new PacketCodec<ContextByteBuf, Object>() {
            @Override
            public Object decode(ContextByteBuf buf) {
                var data = PolymerNetworking.getMetadata(buf.clientConnection(), ServerMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE);
                if (data == null || data.intValue() != PROTOCOL) {
                    buf.skipBytes(buf.readableBytes());
                    return PolymerNoOpPayload.INSTANCE;
                }

                return codec.decode(buf);
            }

            @Override
            public void encode(ContextByteBuf buf, Object value) {
                c.encode(buf, value);
            }
        };
    }

    static boolean clientCheck() {
        if (PolymerImpl.IS_CLIENT) {
            return InternalClientRegistry.enabled;
        }

        return true;
    }
}
