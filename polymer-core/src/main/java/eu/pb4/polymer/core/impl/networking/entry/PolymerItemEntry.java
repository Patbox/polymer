package eu.pb4.polymer.core.impl.networking.entry;

import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerItemEntry(int numId, Identifier identifier, ItemStack representation) {
    public static final PacketCodec<ContextByteBuf, PolymerItemEntry> CODEC = PacketCodec.of(PolymerItemEntry::write, PolymerItemEntry::read);

    public static PolymerItemEntry of(Item item, ServerPlayNetworkHandler handler, int version) {
        return new PolymerItemEntry(Item.getRawId(item), Registries.ITEM.getId(item), item.getDefaultStack());
    }

    public static PolymerItemEntry read(ContextByteBuf buf) {
        return new PolymerItemEntry(buf.readVarInt(), buf.readIdentifier(), ItemStack.OPTIONAL_PACKET_CODEC.decode(buf));
    }

    public void write(ContextByteBuf buf) {
        buf.writeVarInt(this.numId);

        buf.writeIdentifier(this.identifier);
        ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, this.representation);
    }
}
