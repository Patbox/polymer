package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import java.util.function.BiFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record IdValueEntry(int rawId, Identifier id)  {

    public static final StreamCodec<ContextByteBuf, IdValueEntry> CODEC = StreamCodec.ofMember(IdValueEntry::write, IdValueEntry::read);
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(rawId);
        buf.writeIdentifier(id);
    }

    public static IdValueEntry read(FriendlyByteBuf buf) {
        return new IdValueEntry(buf.readVarInt(), buf.readIdentifier());
    }

    public static <T> T read(FriendlyByteBuf buf, BiFunction<Integer, Identifier, T> function) {
        return function.apply(buf.readVarInt(), buf.readIdentifier());
    }
}
