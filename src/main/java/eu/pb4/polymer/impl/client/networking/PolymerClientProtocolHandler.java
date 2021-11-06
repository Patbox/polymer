package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.InternalClientItemGroup;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.client.interfaces.MutableSearchableContainer;
import eu.pb4.polymer.impl.networking.ClientPackets;
import eu.pb4.polymer.impl.networking.ServerPackets;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockStateEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerEntityEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerItemEntry;
import eu.pb4.polymer.impl.other.EventRunners;
import eu.pb4.polymer.mixin.other.ItemGroupAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
public class PolymerClientProtocolHandler {
    public static void handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        var version = -1;
        try {
            version = buf.readVarInt();
            handle(handler, identifier, version, buf);
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Invalid " + identifier + " (" + version + ") packet received from server!");
            PolymerMod.LOGGER.error(e);
        }
    }

    private static void handle(ClientPlayNetworkHandler handler, Identifier identifier, int version, PacketByteBuf buf) {
        switch (identifier.getPath()) {
            case ServerPackets.HANDSHAKE -> handleHandshake(handler, version, buf);

            case ServerPackets.SYNC_STARTED -> PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN);
            case ServerPackets.SYNC_FINISHED -> PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN);

            case ServerPackets.SYNC_BLOCK -> {
                var size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    var entry = PolymerBlockEntry.read(buf, version);
                    if (entry != null) {
                        InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual()));
                    }
                }
            }

            case ServerPackets.SYNC_ITEM -> {
                var size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    var entry = PolymerItemEntry.read(buf, version);
                    if (entry != null) {
                        var item = new ClientPolymerItem(entry.identifier(), entry.representation(), entry.itemGroup());
                        InternalClientRegistry.ITEMS.set(entry.identifier(), item);
                    }
                }
            }

            case ServerPackets.SYNC_ENTITY -> {
                var size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    var entry = PolymerEntityEntry.read(buf, version);
                    if (entry != null) {
                        var entityType = new ClientPolymerEntityType(entry.identifier(), entry.name());
                        InternalClientRegistry.ENTITY_TYPE.set(entry.identifier(), entityType);
                    }
                }
            }

            case ServerPackets.SYNC_ITEM_GROUP_CLEAR -> InternalClientRegistry.clearTabs(i -> true);

            case ServerPackets.SYNC_ITEM_GROUP_REMOVE -> {
                if (version == 0) {
                    var id = buf.readIdentifier();
                    InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id));
                }
            }

            case ServerPackets.SYNC_ITEM_GROUP_VANILLA -> {
                if (version == 0) {
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
            }

            case ServerPackets.SYNC_ITEM_GROUPS -> {
                if (version == 0) {

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

                    if (array.length >= 0) {
                        System.arraycopy(array, 0, newArray, 0, array.length);
                    }

                    ItemGroupAccessor.setGROUPS(newArray);

                    var group = new InternalClientItemGroup(array.length, id, id.toString(), name, icon, stacks);
                    InternalClientRegistry.ITEM_GROUPS.set(id, group);
                }
            }

            case ServerPackets.SYNC_REBUILD_SEARCH -> {
                if (version == 0) {

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

                    PolymerClientUtils.ON_SEARCH_REBUILD.invoke(EventRunners.RUN);
                }
            }

            case ServerPackets.SYNC_BLOCKSTATE -> {
                var size = buf.readVarInt();

                for (int i = 0; i < size; i++) {
                    var entry = PolymerBlockStateEntry.read(buf, version);
                    if (entry != null) {
                        InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId())), entry.numId());
                    }
                }
            }

            case ServerPackets.SYNC_CLEAR -> InternalClientRegistry.clear();

            case ServerPackets.WORLD_SET_BLOCK_UPDATE -> {
                if (version == 0) {
                    var pos = buf.readBlockPos();
                    var id = buf.readVarInt();
                    var block = InternalClientRegistry.BLOCK_STATES.get(id);

                    var chunk = handler.getWorld().getChunk(pos);

                    if (chunk instanceof ClientBlockStorageInterface storage) {
                        storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                    }
                }
            }

            case ServerPackets.WORLD_CHUNK_SECTION_UPDATE -> {
                if (version == 0) {
                    var sectionPos = buf.readChunkSectionPos();
                    var size = buf.readVarInt();
                    var chunk = handler.getWorld().getChunk(sectionPos.getX(), sectionPos.getZ());
                    var section = chunk.getSection(chunk.sectionCoordToIndex(sectionPos.getY()));

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

            case ServerPackets.WORLD_ENTITY -> {
                if (version == 0) {
                    var id = buf.readVarInt();
                    var polymerId = buf.readIdentifier();

                    var entity = handler.getWorld().getEntityById(id);
                    if (entity != null) {
                        ((ClientEntityExtension) entity).polymer_setId(polymerId);
                    }
                }
            }
        }
    }

    private static void handleHandshake(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            InternalClientRegistry.setVersion(buf.readString(64));

            var size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var id = buf.readIdentifier();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                InternalClientRegistry.CLIENT_PROTOCOL.put(id, ClientPackets.getBestSupported(id, list.elements()));
            }

            PolymerClientProtocol.sendSyncRequest(handler);
        }
    }
}
