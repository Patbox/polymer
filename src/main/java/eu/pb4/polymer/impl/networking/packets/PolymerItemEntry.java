package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@ApiStatus.Internal
public record PolymerItemEntry(Identifier identifier, Identifier itemGroup, ItemStack representation) implements BufferWritable {
    public void write(PacketByteBuf buf, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        buf.writeIdentifier(itemGroup);
        buf.writeItemStack(ServerTranslationUtils.parseFor(handler, representation));
    }

    public static PolymerItemEntry of(Item item, ServerPlayNetworkHandler handler) {
        var group = item.getGroup();

        var groupIdentifier = group != null
                ? group instanceof PolymerItemGroup pGroup
                ? pGroup.getId()
                : new Identifier(group.getName().toLowerCase(Locale.ROOT).replace(":", "__"))
                : InternalServerRegistry.POLYMER_ITEM_GROUP;

        return new PolymerItemEntry(
                Registry.ITEM.getId(item),
                groupIdentifier,
                PolymerItemUtils.getPolymerItemStack(item.getDefaultStack(), handler.player)
        );
    }

    public static PolymerItemEntry read(PacketByteBuf buf) {
        return new PolymerItemEntry(buf.readIdentifier(), buf.readIdentifier(), buf.readItemStack());
    }
}
