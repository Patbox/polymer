package eu.pb4.polymer.impl.client.networking;

import com.google.common.base.Predicates;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.MutableSearchableContainer;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.networking.PolymerPacketIds;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockStateEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerItemEntry;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.search.SearchManager;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientPacketHandler {
    public static void handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        try {
            switch (identifier.getPath()) {
                case PolymerPacketIds.REGISTRY_BLOCK -> {
                    var size = buf.readVarInt();

                    for (int i = 0; i < size; i++) {
                        var entry = PolymerBlockEntry.read(buf);
                        InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual()));
                    }
                }

                case PolymerPacketIds.REGISTRY_ITEM -> {
                    var size = buf.readVarInt();

                    for (int i = 0; i < size; i++) {
                        var entry = PolymerItemEntry.read(buf);
                        var item = new ClientPolymerItem(entry.identifier(), entry.representation(), entry.itemGroup());
                        InternalClientRegistry.ITEMS.set(entry.identifier(), new ClientPolymerItem(entry.identifier(), entry.representation(), entry.itemGroup()));
                        PolymerClientUtils.ON_ITEM_SYNC.invoke((c) -> c.accept(item));
                    }
                }

                case PolymerPacketIds.REGISTRY_ITEM_GROUP_CLEAR -> {
                    InternalClientRegistry.clearTabs(Predicates.alwaysTrue());
                }

                case PolymerPacketIds.REGISTRY_ITEM_GROUP_REMOVE -> {
                    var id = buf.readIdentifier();
                    InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id));
                }

                case PolymerPacketIds.REGISTRY_ITEM_GROUP_VANILLA -> {
                    var id = buf.readString();
                    ItemGroup group = InternalClientRegistry.VANILLA_ITEM_GROUPS.get(id);

                    if (group != null) {
                        var groupAccess = (ClientItemGroupExtension) group;
                        groupAccess.polymer_clearStacks();

                        var size = buf.readVarInt();

                        for (int i = 0; i < size; i++) {
                            groupAccess.polymer_addStack(buf.readItemStack());
                        }
                    }
                }

                case PolymerPacketIds.REGISTRY_ITEM_GROUP -> {
                    var id = buf.readIdentifier();
                    var name = buf.readText();
                    var icon = buf.readItemStack();

                    var size = buf.readVarInt();

                    var stacks = new ArrayList<ItemStack>();
                    for (int i = 0; i < size; i++) {
                        stacks.add(buf.readItemStack());
                    }

                    var array = ItemGroupAccessor.getGROUPS();

                    var newArray = new ItemGroup[array.length + 1];

                    for (int i = 0; i < array.length; i++) {
                        newArray[i] = array[i];
                    }

                    ItemGroupAccessor.setGROUPS(newArray);

                    var group = new InternalClientItemGroup(array.length, id, id.toString(), name, icon, stacks);
                    InternalClientRegistry.ITEM_GROUPS.set(id, group);
                }

                case PolymerPacketIds.REGISTRY_RESET_SEARCH -> {
                    var a = MinecraftClient.getInstance().getSearchableContainer(SearchManager.ITEM_TOOLTIP);
                    var b = MinecraftClient.getInstance().getSearchableContainer(SearchManager.ITEM_TAG);

                    ((MutableSearchableContainer) a).polymer_removeIf((s) -> s instanceof ItemStack stack && PolymerItemUtils.getPolymerIdentifier(stack) != null);
                    ((MutableSearchableContainer) b).polymer_removeIf((s) -> s instanceof ItemStack stack && PolymerItemUtils.getPolymerIdentifier(stack) != null);

                    for (var group : ItemGroup.GROUPS) {
                        if (group == ItemGroup.SEARCH) {
                            continue;
                        }

                        Collection<ItemStack> stacks;

                        if (group instanceof InternalClientItemGroup clientItemGroup) {
                            stacks = clientItemGroup.getStacks();
                        } else {
                            stacks = ((ClientItemGroupExtension) group).polymer_getStacks();
                        }

                        if (stacks != null) {
                            for (var stack : stacks) {
                                a.add(stack);
                                b.add(stack);
                            }
                        }
                    }

                    a.reload();
                    b.reload();

                    PolymerClientUtils.ON_SEARCH_REBUILD.invoke((r) -> r.run());
                }

                case PolymerPacketIds.REGISTRY_BLOCKSTATE -> {
                    var size = buf.readVarInt();

                    for (int i = 0; i < size; i++) {
                        var entry = PolymerBlockStateEntry.read(buf);
                        InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId())), entry.numId());
                    }
                }

                case PolymerPacketIds.REGISTRY_CLEAR -> {
                    InternalClientRegistry.clear();
                }

                case PolymerPacketIds.BLOCK_UPDATE -> {
                    var pos = buf.readBlockPos();
                    var id = buf.readVarInt();
                    var block = InternalClientRegistry.BLOCK_STATES.get(id);

                    var chunk = handler.getWorld().getChunk(pos);

                    if (chunk instanceof ClientBlockStorageInterface storage) {
                        storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                    }
                }

                case PolymerPacketIds.CHUNK_SECTION_UPDATE -> {
                    var sectionPos = buf.readChunkSectionPos();
                    var size = buf.readVarInt();
                    var section = handler.getWorld().getChunk(sectionPos.getX(), sectionPos.getZ()).getSection(sectionPos.getY());

                    if (section instanceof ClientBlockStorageInterface storage) {
                        for (int i = 0; i < size; i++) {
                            long value = buf.readVarLong();
                            var pos = (short) ((int) (value & 4095L));
                            var block = InternalClientRegistry.BLOCK_STATES.get((int) (value >>> 12));

                            storage.polymer_setClientPolymerBlock(ChunkSectionPos.unpackLocalX(pos), ChunkSectionPos.unpackLocalY(pos), ChunkSectionPos.unpackLocalZ(pos), block);
                        }
                    }
                }
            }
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Invalid " + identifier + " packet received from server!");
            PolymerMod.LOGGER.error(e);
        }
    }
}
