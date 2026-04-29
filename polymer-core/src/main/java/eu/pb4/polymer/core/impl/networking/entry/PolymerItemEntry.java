package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerItemEntry(int numId, Identifier identifier, ItemStack representation) {
    public static final StreamCodec<ContextByteBuf, PolymerItemEntry> CODEC = StreamCodec.ofMember(PolymerItemEntry::write, PolymerItemEntry::read);

    public static PolymerItemEntry of(Item item, ServerGamePacketListenerImpl handler, int version) {
        return new PolymerItemEntry(Item.getId(item), BuiltInRegistries.ITEM.getKey(item), item.getDefaultInstance());
    }

    public static PolymerItemEntry read(ContextByteBuf buf) {
        var bufId = buf.readVarInt();
        var id = buf.readIdentifier();
        try {
            return new PolymerItemEntry(bufId, id, ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
        } catch (Throwable e) {
            PolymerImpl.LOGGER.error("Failed to parse '{}' item! Invalid stack data!", id);
            throw e;
        }
    }

    public void write(ContextByteBuf buf) {
        buf.writeVarInt(this.numId);

        buf.writeIdentifier(this.identifier);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, this.representation);
    }
}
