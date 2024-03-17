package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PolymerTagEntry(Identifier registry, List<TagData> tags) {

    public static final PacketCodec<ContextByteBuf, PolymerTagEntry> CODEC = PacketCodec.of(PolymerTagEntry::write, PolymerTagEntry::read);

    public static PolymerTagEntry of(Registry<Object> registry, ServerPlayNetworkHandler handler, int version) {
        if (registry instanceof RegistryExtension && !((RegistryExtension<Object>) registry).polymer$getEntries().isEmpty()) {
            var registryExtension = (RegistryExtension<Object>) registry;

            var out = new ArrayList<TagData>();
            for (var entry : registryExtension.polymer$getTagsInternal().values()){
                var ids = new IntArrayList();

                for (var obj : entry) {
                    if (PolymerUtils.isServerOnly(obj.value())) {
                        ids.add(registry.getRawId(obj.value()));
                    }
                }

                if (!ids.isEmpty()) {
                    out.add(new TagData(entry.getTag().id(), ids));
                }
            }

            return out.isEmpty() ? null : new PolymerTagEntry(registry.getKey().getValue(), out);
        }
        return null;
    }

    public static PolymerTagEntry read(PacketByteBuf buf) {
        var registry = buf.readIdentifier();
        var size = buf.readVarInt();

        var tags = new ArrayList<TagData>();
        for (int i = 0; i < size; i++) {
            var tagId = buf.readIdentifier();
            var sizeIds = buf.readVarInt();
            var idList = new IntArrayList(sizeIds);
            for (int a = 0; a < sizeIds; a++) {
                idList.add(buf.readVarInt());
            }
            tags.add(new TagData(tagId, idList));
        }

        return new PolymerTagEntry(registry, tags);
    }

    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.registry);
        buf.writeVarInt(this.tags.size());

        for (var tag : this.tags) {
            buf.writeIdentifier(tag.id);
            buf.writeVarInt(tag.ids.size());
            for (var id : tag.ids) {
                buf.writeVarInt(id);
            }
        }
    }


    public record TagData(Identifier id, IntList ids) {}
}
