package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.networking.entry.IdValueEntry;
import eu.pb4.polymer.core.impl.networking.entry.PolymerTagEntry;
import eu.pb4.polymer.core.impl.networking.entry.WritableEntry;
import eu.pb4.polymer.networking.api.payload.SingleplayerSerialization;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PolymerGenericListPayload<T extends WritableEntry>(Identifier id, List<T> entries) implements VersionedPayload, SingleplayerSerialization {
    public static final Map<Identifier, WritableEntry.Reader> ENTRIES = new HashMap<>();

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (var entry : entries) {
            entry.write(buf, version);
        }
    }

    public static PolymerGenericListPayload<?> read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        var reader = ENTRIES.getOrDefault(identifier, IdValueEntry::read);
        var list = new ArrayList<WritableEntry>();
        var count = buf.readVarInt();

        for (int i = 0; i < count; i++) {
            list.add(reader.read(buf, version));
        }

        return new PolymerGenericListPayload<WritableEntry>(identifier, list);
    }
}
