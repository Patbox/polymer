package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public record PolymerItemGroupContentAddS2CPayload(Identifier groupId, List<Entry> stacksMain, List<Entry> stacksSearch) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerItemGroupContentAddS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);
    public static final StreamCodec<ContextByteBuf, PolymerItemGroupContentAddS2CPayload> CODEC = StreamCodec.ofMember(PolymerItemGroupContentAddS2CPayload::write, PolymerItemGroupContentAddS2CPayload::read);
    public static PolymerItemGroupContentAddS2CPayload of(int version, CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        List<Entry> entryMain;
        List<Entry> entrySearch;

        var contents = PolymerItemGroupUtils.getContentsFor(handler.player, group);

        if (PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            entryMain = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, List.copyOf(contents.main())));
            entrySearch = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, List.copyOf(contents.search())));
        } else if (version == 9) {
            var ctx = handler.getPacketContext();
            var stackMain = new ArrayList<ItemStack>();
            var stackSearch = new ArrayList<ItemStack>();

            entryMain = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, stackMain));
            entrySearch = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, stackSearch));

            for (var item : contents.main()) {
                if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(BuiltInRegistries.ITEM, item.getItem())) {
                    stackMain.add(item);
                }
            }

            for (var item : contents.search()) {
                if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(BuiltInRegistries.ITEM, item.getItem())) {
                    stackSearch.add(item);
                }
            }
        } else {
            var ctx = handler.getPacketContext();
            entryMain = new ArrayList<>();
            entrySearch = new ArrayList<>();

            groupEntries(entryMain, contents.main(), ctx);
            groupEntries(entrySearch, contents.search(), ctx);
        }

        return new PolymerItemGroupContentAddS2CPayload(PolymerItemGroupUtils.getId(group), entryMain, entrySearch);
    }

    private static void groupEntries(List<Entry> entry, Collection<ItemStack> main, PacketContext ctx) {
        var stacks = new ArrayList<ItemStack>();

        ItemStack previous = ItemStack.EMPTY;
        for (var item : main) {
            if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(BuiltInRegistries.ITEM, item.getItem())) {
                stacks.add(item);
            } else {
                if (!stacks.isEmpty()) {
                    var mode = previous.isEmpty() ? Mode.INSERT_BEGINNING : Mode.RELATIVE;
                    entry.add(new Entry(mode, previous, stacks));
                    stacks = new ArrayList<>();
                }

                previous = item;
            }
        }
        if (!stacks.isEmpty()) {
            var mode = previous.isEmpty() ? Mode.INSERT_END : Mode.RELATIVE;
            entry.add(new Entry(mode, previous, stacks));
        }
    }

    public void write(ContextByteBuf buf) {
        buf.writeIdentifier(this.groupId);

        if (buf.version() == 9) {
            ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, this.stacksMain.isEmpty() ? List.of() : this.stacksMain.getFirst().stacks());
            ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, this.stacksSearch.isEmpty() ? List.of() : this.stacksSearch.getFirst().stacks());
            return;
        }

        Entry.LIST_PACKET_CODEC.encode(buf, this.stacksMain);
        Entry.LIST_PACKET_CODEC.encode(buf, this.stacksSearch);
    }

    public boolean isNonEmpty() {
        return !this.stacksMain.isEmpty() || !this.stacksSearch.isEmpty();
    }

    public static PolymerItemGroupContentAddS2CPayload read(ContextByteBuf buf) {
        if (buf.version() == 9) {
            return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                    List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf))),
                    List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf)))
            );
        }
        return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                Entry.LIST_PACKET_CODEC.decode(buf),
                Entry.LIST_PACKET_CODEC.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public record Entry(Mode mode, ItemStack relative, List<ItemStack> stacks) {
        public static final StreamCodec<ContextByteBuf, Entry> PACKET_CODEC = StreamCodec.of(Entry::write, Entry::read);

        private static Entry read(ContextByteBuf byteBuf) {
            var mode = Mode.values()[byteBuf.readVarInt()];
            var stack = ItemStack.EMPTY;
            if (mode == Mode.RELATIVE) {
                stack = ItemStack.STREAM_CODEC.decode(byteBuf);
            }
            var list = ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(byteBuf);
            return new Entry(mode, stack, list);
        }

        private static void write(ContextByteBuf byteBuf, Entry entry) {
            byteBuf.writeVarInt(entry.mode.ordinal());
            if (entry.mode == Mode.RELATIVE) {
                ItemStack.STREAM_CODEC.encode(byteBuf, entry.relative);
            }
            ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(byteBuf, entry.stacks);
        }

        public static final StreamCodec<ContextByteBuf, List<Entry>> LIST_PACKET_CODEC = PACKET_CODEC.apply(ByteBufCodecs.list());
    }

    public enum Mode {
        RELATIVE,
        INSERT_BEGINNING,
        INSERT_END,
    }
}
