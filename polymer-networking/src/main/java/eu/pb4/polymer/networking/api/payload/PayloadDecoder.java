package eu.pb4.polymer.networking.api.payload;

import eu.pb4.polymer.networking.impl.UnhandledPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface PayloadDecoder<T> {
    @Nullable
    T readPacket(Identifier identifier, PacketByteBuf buf);

    default PacketByteBuf.PacketReader<? extends CustomPayload> forPacket(Identifier identifier) {
        return (b) -> {
            try {
                var payload = readPacket(identifier, b);
                if (payload != null) {
                    return (CustomPayload) payload;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to decode packet '" + identifier +"'", e);
            }

            b.skipBytes(b.readableBytes());
            return new UnhandledPayload(identifier);
        };
    }
}
