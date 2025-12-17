package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public record PolymerTagEntry(Identifier registry, List<TagData> tags) {

    public static final StreamCodec<ContextByteBuf, PolymerTagEntry> CODEC = StreamCodec.ofMember(PolymerTagEntry::write, PolymerTagEntry::read);

    public static PolymerTagEntry of(Registry<Object> registry, ServerGamePacketListenerImpl handler, int version) {
        if (registry instanceof RegistryExtension && !((RegistryExtension<Object>) registry).polymer$getEntries().isEmpty()) {
            var registryExtension = (RegistryExtension<Object>) registry;

            var out = new ArrayList<TagData>();
            for (var entry : registryExtension.polymer$getTagsInternal().values()){
                var ids = new IntArrayList();

                for (var obj : entry) {
                    if (PolymerImplUtils.isServerSideSyncableEntry(registry, obj.value())) {
                        ids.add(registry.getId(obj.value()));
                    }
                }

                if (!ids.isEmpty()) {
                    out.add(new TagData(entry.key().location(), ids));
                }
            }

            return out.isEmpty() ? null : new PolymerTagEntry(registry.key().identifier(), out);
        }
        return null;
    }

    public static PolymerTagEntry read(FriendlyByteBuf buf) {
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

    public void write(FriendlyByteBuf buf) {
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
