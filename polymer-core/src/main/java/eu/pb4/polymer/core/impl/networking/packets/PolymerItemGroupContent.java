package eu.pb4.polymer.core.impl.networking.packets;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.networking.api.ServerPacketWriter;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record PolymerItemGroupContent(Identifier identifier, List<ItemStack> stacksMain, List<ItemStack> stacksSearch) implements ServerPacketWriter {
    public static PolymerItemGroupContent of(ItemGroup group, ServerPlayNetworkHandler handler) {
        List<ItemStack> stacksMain;
        List<ItemStack> stacksSearch;

        var contents = PolymerItemGroupUtils.getContentsFor(handler.player, group);

        if (PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            stacksMain = List.copyOf(contents.main());
            stacksSearch = List.copyOf(contents.search());
        } else {
            stacksMain = new ArrayList<>();
            stacksSearch = new ArrayList<>();
            for (var item : contents.main()) {
                if (PolymerItemUtils.isPolymerServerItem(item, handler.player) || PolymerImplUtils.isServerSideSyncableEntry(Registries.ITEM, item.getItem())) {
                    stacksMain.add(item);
                }
            }

            for (var item : contents.search()) {
                if (PolymerItemUtils.isPolymerServerItem(item, handler.player) || PolymerImplUtils.isServerSideSyncableEntry(Registries.ITEM, item.getItem())) {
                    stacksSearch.add(item);
                }
            }
        }

        return new PolymerItemGroupContent(PolymerItemGroupUtils.getId(group), stacksMain, stacksSearch);
    }

    @Override
    public void write(ServerPlayNetworkHandler handler, PacketByteBuf buf, Identifier packetId, int version) {
        buf.writeIdentifier(this.identifier);
        buf.writeVarInt(this.stacksMain.size());
        for (var item : this.stacksMain) {
            PolymerImplUtils.writeStack(buf, PolymerImplUtils.convertStack(item, handler.player, PolymerUtils.getCreativeTooltipContext(handler.player)));
        }

        buf.writeVarInt(this.stacksSearch.size());
        for (var item : this.stacksSearch) {
            PolymerImplUtils.writeStack(buf, PolymerImplUtils.convertStack(item, handler.player, PolymerUtils.getCreativeTooltipContext(handler.player)));
        }
    }

    public boolean isNonEmpty() {
        return !this.stacksMain.isEmpty() || !this.stacksSearch.isEmpty();
    }
}
