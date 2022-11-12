package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record PolymerVanillaItemGroupEntry(String identifier, List<ItemStack> stacks) implements BufferWritable {
    public static PolymerVanillaItemGroupEntry of(ItemGroup group, ServerPlayNetworkHandler handler) {
        var stacks = new ArrayList<ItemStack>();

        for (var item : group.getDisplayStacks()) {
            if (PolymerItemUtils.isPolymerServerItem(item)) {
                stacks.add(PolymerItemUtils.getPolymerItemStack(item, handler.player));
            }
        }

        return new PolymerVanillaItemGroupEntry("vanilla_" + group.getType().name().toLowerCase(Locale.ROOT) + "_" + group.getRow() + "_" + group.getColumn(), stacks);
    }

    @Override
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeString(identifier);
        buf.writeVarInt(stacks.size());
        for (var item : stacks) {
            PolymerImplUtils.writeStack(buf, ServerTranslationUtils.parseFor(handler, item));
        }
    }
}
