package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.codec.StreamCodec;

public interface PayloadUtil {
    int PROTOCOL = SharedConstants.getProtocolVersion();

    @SuppressWarnings("unchecked")
    static <T> StreamCodec<ContextByteBuf, T> protocolSecured(StreamCodec<ContextByteBuf, T> codec) {
        var c = (StreamCodec<ContextByteBuf, Object>) codec;
        return (StreamCodec<ContextByteBuf, T>) (Object) new StreamCodec<ContextByteBuf, Object>() {
            @Override
            public Object decode(ContextByteBuf buf) {
                var data = PolymerNetworking.getMetadata(buf.clientConnection(), ServerMetadataKeys.MINECRAFT_PROTOCOL, IntTag.TYPE);
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
