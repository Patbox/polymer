package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;

public record PolymerItemGroupContentAddS2CPayload(Identifier groupId, List<ItemStack> stacksMain, List<ItemStack> stacksSearch) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupContentAddS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);
    public static final PacketCodec<ContextByteBuf, PolymerItemGroupContentAddS2CPayload> CODEC = PacketCodec.of(PolymerItemGroupContentAddS2CPayload::write, PolymerItemGroupContentAddS2CPayload::read);

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

    public void write(ContextByteBuf buf) {
        buf.writeIdentifier(this.groupId);

        ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, this.stacksMain);
        ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, this.stacksSearch);
    }

    public boolean isNonEmpty() {
        return !this.stacksMain.isEmpty() || !this.stacksSearch.isEmpty();
    }

    public static PolymerItemGroupContentAddS2CPayload read(ContextByteBuf buf) {
        return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf),
                ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf)
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
