package eu.pb4.polymer.networking.api.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface PayloadDecoder<T> {
    @Nullable
    T readPacket(Identifier identifier, PacketByteBuf buf);

    default PacketByteBuf.PacketReader<? extends CustomPayload> forPacket(Identifier identifier) {
        return (b) -> (CustomPayload) readPacket(identifier, b);
    }
}
