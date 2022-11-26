package eu.pb4.polymer.core.impl.networking.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.function.BiFunction;

public record IdValueEntry(int rawId, Identifier id) implements BufferWritable {
    @Override
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeVarInt(rawId);
        buf.writeIdentifier(id);
    }

    public static IdValueEntry read(PacketByteBuf buf, int _unused) {
        return new IdValueEntry(buf.readVarInt(), buf.readIdentifier());
    }

    public static <T> T read(PacketByteBuf buf, BiFunction<Integer, Identifier, T> function) {
        return function.apply(buf.readVarInt(), buf.readIdentifier());
    }
}
