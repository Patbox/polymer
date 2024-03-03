package eu.pb4.polymer.networking.api.payload;

import eu.pb4.polymer.networking.impl.ExtClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface ContextPayload extends CustomPayload {
    void write(PacketContext context, PacketByteBuf buf);

    default void write(PacketByteBuf buf) {
        write(PacketContext.get(), buf);
    }

    interface Decoder<T extends ContextPayload> extends PayloadDecoder<T> {
        @Nullable
        T readPacket(PacketContext context, Identifier identifier, PacketByteBuf buf);

        @Override
        @Nullable
        default T readPacket(Identifier identifier, PacketByteBuf buf) {
            return readPacket(PacketContext.get(), identifier, buf);
        };
    }
}
