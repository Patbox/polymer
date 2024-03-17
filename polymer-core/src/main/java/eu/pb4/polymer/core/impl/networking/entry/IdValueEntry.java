package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

public record IdValueEntry(int rawId, Identifier id)  {

    public static final PacketCodec<ContextByteBuf, IdValueEntry> CODEC = PacketCodec.of(IdValueEntry::write, IdValueEntry::read);
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(rawId);
        buf.writeIdentifier(id);
    }

    public static IdValueEntry read(PacketByteBuf buf) {
        return new IdValueEntry(buf.readVarInt(), buf.readIdentifier());
    }

    public static <T> T read(PacketByteBuf buf, BiFunction<Integer, Identifier, T> function) {
        return function.apply(buf.readVarInt(), buf.readIdentifier());
    }
}
