package eu.pb4.polymer.core.impl.networking.packets;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PolymerItemGroupContent(Identifier identifier, List<ItemStack> stacksMain, List<ItemStack> stacksSearch) implements BufferWritable {
    public static PolymerItemGroupContent of(ItemGroup group, ServerPlayNetworkHandler handler) {
        var stacksMain = new ArrayList<ItemStack>();
        var stacksSearch = new ArrayList<ItemStack>();

        var anyContent = PolymerItemGroupUtils.isPolymerItemGroup(group);
        var contents = PolymerItemGroupUtils.getContentsFor(handler.player, group);

        for (var item : contents.main()) {
            if (anyContent || PolymerItemUtils.isPolymerServerItem(item)) {
                stacksMain.add(PolymerItemUtils.getPolymerItemStack(item, PolymerUtils.getCreativeTooltipContext(handler.player), handler.player));
            }
        }

        for (var item : contents.search()) {
            if (anyContent || PolymerItemUtils.isPolymerServerItem(item)) {
                stacksSearch.add(PolymerItemUtils.getPolymerItemStack(item, PolymerUtils.getCreativeTooltipContext(handler.player), handler.player));
            }
        }

        return new PolymerItemGroupContent(PolymerImplUtils.toItemGroupId(group), stacksMain, stacksSearch);
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
