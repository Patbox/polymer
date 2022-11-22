package eu.pb4.polymer.impl.networking.packets;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PolymerVanillaItemGroupEntry(Identifier identifier, List<ItemStack> stacksMain, List<ItemStack> stacksSearch) implements BufferWritable {
    public static PolymerVanillaItemGroupEntry of(ItemGroup group, ServerPlayNetworkHandler handler) {
        var stacksMain = new ArrayList<ItemStack>();
        var stacksSearch = new ArrayList<ItemStack>();

        for (var item : group.getDisplayStacks()) {
            if (PolymerItemUtils.isPolymerServerItem(item)) {
                stacksMain.add(PolymerItemUtils.getPolymerItemStack(item, handler.player));
            }
        }

        for (var item : group.getSearchTabStacks()) {
            if (PolymerItemUtils.isPolymerServerItem(item)) {
                stacksSearch.add(PolymerItemUtils.getPolymerItemStack(item, handler.player));
            }
        }

        return new PolymerVanillaItemGroupEntry(PolymerImplUtils.toItemGroupId(group), stacksMain, stacksSearch);
    }

    @Override
    public void write(PacketByteBuf buf, int version, ServerPlayNetworkHandler handler) {
        buf.writeIdentifier(identifier);
        buf.writeVarInt(stacksMain.size());
        for (var item : stacksMain) {
            PolymerImplUtils.writeStack(buf, ServerTranslationUtils.parseFor(handler, item));
        }

        buf.writeVarInt(stacksSearch.size());
        for (var item : stacksSearch) {
            PolymerImplUtils.writeStack(buf, ServerTranslationUtils.parseFor(handler, item));
        }
    }

    public boolean isNonEmpty() {
        return !this.stacksMain.isEmpty() || !this.stacksSearch.isEmpty();
    }
}
