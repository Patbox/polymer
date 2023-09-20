package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.SingleplayerSerialization;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public record PolymerItemGroupContentAddS2CPayload(Identifier groupId, List<ItemStack> stacksMain, List<ItemStack> stacksSearch) implements VersionedPayload, SingleplayerSerialization {
    public static final Identifier ID = S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD;
    public static PolymerItemGroupContentAddS2CPayload of(ItemGroup group, ServerPlayNetworkHandler handler) {
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

        return new PolymerItemGroupContentAddS2CPayload(PolymerItemGroupUtils.getId(group), stacksMain, stacksSearch);
    }

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);
        buf.writeCollection(this.stacksMain, PacketByteBuf::writeItemStack);
        buf.writeCollection(this.stacksSearch, PacketByteBuf::writeItemStack);
    }

    public boolean isNonEmpty() {
        return !this.stacksMain.isEmpty() || !this.stacksSearch.isEmpty();
    }

    public static PolymerItemGroupContentAddS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                buf.readCollection(ArrayList::new, PacketByteBuf::readItemStack),
                buf.readCollection(ArrayList::new, PacketByteBuf::readItemStack)
        );
    }
    @Override
    public Identifier id() {
        return ID;
    }
}
