package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record PolymerItemEntry(Identifier identifier, String itemGroup, ItemStack representation) implements BufferWritable {
    public void write(PacketByteBuf buf, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        buf.writeString(itemGroup);
        buf.writeItemStack(ServerTranslationUtils.parseFor(handler, representation));
    }

    public static PolymerItemEntry of(Item item, ServerPlayNetworkHandler handler) {
        var group = item.getGroup();

        var groupIdentifier = group != null
                ? group instanceof PolymerItemGroup pGroup
                ? pGroup.getId().toString()
                : ((ItemGroupAccessor) group).getId()
                : InternalServerRegistry.POLYMER_ITEM_GROUP.toString();

        return new PolymerItemEntry(
                Registry.ITEM.getId(item),
                groupIdentifier,
                PolymerItemUtils.getPolymerItemStack(item.getDefaultStack(), handler.player)
        );
    }

    public static PolymerItemEntry read(PacketByteBuf buf) {
        return new PolymerItemEntry(buf.readIdentifier(), buf.readString(), buf.readItemStack());
    }
}
