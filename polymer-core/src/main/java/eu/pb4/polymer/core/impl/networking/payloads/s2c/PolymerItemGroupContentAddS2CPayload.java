package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record PolymerItemGroupContentAddS2CPayload(Identifier groupId, List<Entry> stacksMain, List<Entry> stacksSearch) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupContentAddS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);
    public static final PacketCodec<ContextByteBuf, PolymerItemGroupContentAddS2CPayload> CODEC = PacketCodec.of(PolymerItemGroupContentAddS2CPayload::write, PolymerItemGroupContentAddS2CPayload::read);
    public static PolymerItemGroupContentAddS2CPayload of(int version, ItemGroup group, ServerPlayNetworkHandler handler) {
        List<Entry> entryMain;
        List<Entry> entrySearch;

        var contents = PolymerItemGroupUtils.getContentsFor(handler.player, group);

        if (PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            entryMain = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, List.copyOf(contents.main())));
            entrySearch = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, List.copyOf(contents.search())));
        } else if (version == 9) {
            var ctx = PacketContext.create(handler);
            var stackMain = new ArrayList<ItemStack>();
            var stackSearch = new ArrayList<ItemStack>();

            entryMain = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, stackMain));
            entrySearch = List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, stackSearch));

            for (var item : contents.main()) {
                if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(Registries.ITEM, item.getItem())) {
                    stackMain.add(item);
                }
            }

            for (var item : contents.search()) {
                if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(Registries.ITEM, item.getItem())) {
                    stackSearch.add(item);
                }
            }
        } else {
            var ctx = PacketContext.create(handler);
            entryMain = new ArrayList<>();
            entrySearch = new ArrayList<>();

            groupEntries(entryMain, contents.main(), ctx);
            groupEntries(entrySearch, contents.search(), ctx);
        }

        return new PolymerItemGroupContentAddS2CPayload(PolymerItemGroupUtils.getId(group), entryMain, entrySearch);
    }

    private static void groupEntries(List<Entry> entry, Collection<ItemStack> main, PacketContext.NotNullWithPlayer ctx) {
        var stacks = new ArrayList<ItemStack>();

        ItemStack previous = ItemStack.EMPTY;
        for (var item : main) {
            if (PolymerItemUtils.isPolymerServerItem(item, ctx) || PolymerImplUtils.isServerSideSyncableEntry(Registries.ITEM, item.getItem())) {
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
            ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, this.stacksMain.isEmpty() ? List.of() : this.stacksMain.getFirst().stacks());
            ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, this.stacksSearch.isEmpty() ? List.of() : this.stacksSearch.getFirst().stacks());
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
                    List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf))),
                    List.of(new Entry(Mode.INSERT_END, ItemStack.EMPTY, ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf)))
            );
        }
        return new PolymerItemGroupContentAddS2CPayload(buf.readIdentifier(),
                Entry.LIST_PACKET_CODEC.decode(buf),
                Entry.LIST_PACKET_CODEC.decode(buf)
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record Entry(Mode mode, ItemStack relative, List<ItemStack> stacks) {
        public static final PacketCodec<ContextByteBuf, Entry> PACKET_CODEC = PacketCodec.ofStatic(Entry::write, Entry::read);

        private static Entry read(ContextByteBuf byteBuf) {
            var mode = Mode.values()[byteBuf.readVarInt()];
            var stack = ItemStack.EMPTY;
            if (mode == Mode.RELATIVE) {
                stack = ItemStack.PACKET_CODEC.decode(byteBuf);
            }
            var list = ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(byteBuf);
            return new Entry(mode, stack, list);
        }

        private static void write(ContextByteBuf byteBuf, Entry entry) {
            byteBuf.writeVarInt(entry.mode.ordinal());
            if (entry.mode == Mode.RELATIVE) {
                ItemStack.PACKET_CODEC.encode(byteBuf, entry.relative);
            }
            ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(byteBuf, entry.stacks);
        }

        public static final PacketCodec<ContextByteBuf, List<Entry>> LIST_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs.toList());
    }

    public enum Mode {
        RELATIVE,
        INSERT_BEGINNING,
        INSERT_END,
    }
}
