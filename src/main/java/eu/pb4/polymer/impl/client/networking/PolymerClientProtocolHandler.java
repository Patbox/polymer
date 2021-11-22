package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolymerClientProtocolHandler {
    public static void handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        if (PolymerImpl.ENABLE_NETWORKING_CLIENT) {
            var version = -1;
            try {
                version = buf.readVarInt();
                if (!handle(handler, identifier.getPath(), version, buf)) {
                    PolymerImpl.LOGGER.warn("Unsupported packet " + identifier + " (" + version + ") was received from server!");
                }
            } catch (Exception e) {
                PolymerImpl.LOGGER.error("Invalid " + identifier + " (" + version + ") packet received from server!");
                PolymerImpl.LOGGER.error(e);
            }
        }
    }

    private static boolean handle(ClientPlayNetworkHandler handler, String packet, int version, PacketByteBuf buf) {
        return switch (packet) {
            case ServerPackets.HANDSHAKE -> handleHandshake(handler, version, buf);

            case ServerPackets.SYNC_STARTED -> run(() -> PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN));
            case ServerPackets.SYNC_FINISHED -> run(() -> PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN));
            case ServerPackets.SYNC_BLOCK -> handleGenericSync(handler, version, buf, PolymerBlockEntry::read,
                    (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual())));
            case ServerPackets.SYNC_ITEM -> handleGenericSync(handler, version, buf, PolymerItemEntry::read,
                    (entry) -> InternalClientRegistry.ITEMS.set(entry.identifier(),
                            new ClientPolymerItem(
                                    entry.identifier(),
                                    entry.representation(),
                                    entry.itemGroup(),
                                    entry.foodLevels(),
                                    entry.saturation(),
                                    entry.miningTool(),
                                    entry.miningLevel()
                            )));
            case ServerPackets.SYNC_BLOCKSTATE -> handleGenericSync(handler, version, buf, PolymerBlockStateEntry::read,
                    (entry) -> InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId())), entry.numId()));
            case ServerPackets.SYNC_ENTITY -> handleGenericSync(handler, version, buf, PolymerEntityEntry::read,
                    (entry) -> InternalClientRegistry.ENTITY_TYPE.set(entry.identifier(), new ClientPolymerEntityType(entry.identifier(), entry.name())));
            case ServerPackets.SYNC_ITEM_GROUP -> handleItemGroupSync(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_CLEAR -> run(() -> InternalClientRegistry.clearTabs(i -> true));
            case ServerPackets.SYNC_ITEM_GROUP_REMOVE -> handleItemGroupRemove(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_VANILLA -> handleItemGroupVanillaSync(handler, version, buf);
            case ServerPackets.SYNC_REBUILD_SEARCH -> handleSearchRebuild(handler, version, buf);
            case ServerPackets.SYNC_CLEAR -> run(InternalClientRegistry::clear);

            case ServerPackets.WORLD_SET_BLOCK_UPDATE -> handleSetBlock(handler, version, buf);
            case ServerPackets.WORLD_CHUNK_SECTION_UPDATE -> handleWorldSectionUpdate(handler, version, buf);
            case ServerPackets.WORLD_ENTITY -> handleEntity(handler, version, buf);
            default -> false;
        };
    }

    private static boolean run(Runnable runnable) {
        runnable.run();
        return true;
    }

    private static boolean handleItemGroupRemove(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var id = buf.readIdentifier();
            InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id));
            return true;
        }

        return false;
    }

    private static boolean handleItemGroupVanillaSync(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
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
            return true;
        }
        return false;
    }

    private static boolean handleEntity(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var id = buf.readVarInt();
            var polymerId = buf.readIdentifier();

            var entity = handler.getWorld().getEntityById(id);
            if (entity != null) {
                ((ClientEntityExtension) entity).polymer_setId(polymerId);
            }
            return true;
        }
        return false;
    }

    private static boolean handleSetBlock(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var pos = buf.readBlockPos();
            var id = buf.readVarInt();
            var block = InternalClientRegistry.BLOCK_STATES.get(id);

            var chunk = handler.getWorld().getChunk(pos);

            if (chunk instanceof ClientBlockStorageInterface storage) {
                storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), block);
            }
            return true;
        }
        return false;
    }

    private static boolean handleWorldSectionUpdate(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
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
            return true;
        }
        return false;
    }

    private static boolean handleSearchRebuild(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
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
            return true;
        }
        return false;
    }

    private static boolean handleItemGroupSync(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
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

            return true;
        }
        return false;
    }

    private static boolean handleHandshake(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            InternalClientRegistry.setVersion(buf.readString(64));

            var size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var id = buf.readString();

                var size2 = buf.readVarInt();
                var list = new IntArrayList();

                for (int i2 = 0; i2 < size2; i2++) {
                    list.add(buf.readVarInt());
                }

                InternalClientRegistry.CLIENT_PROTOCOL.put(id, ClientPackets.getBestSupported(id, list.elements()));
            }

            PolymerClientUtils.ON_HANDSHAKE.invoke(EventRunners.RUN);
            PolymerClientProtocol.sendTooltipContext(handler);
            PolymerClientProtocol.sendSyncRequest(handler);

            return true;
        }
        return false;
    }


    private static <T> boolean handleGenericSync(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf, EntryReader<T> reader, Consumer<T> entryConsumer) {
        var size = buf.readVarInt();

        for (int i = 0; i < size; i++) {
            var entry = reader.read(buf, version);
            if (entry != null) {
                entryConsumer.accept(entry);
            }
        }

        return true;
    }

    interface EntryReader<T> {
        @Nullable
        T read(PacketByteBuf buf, int version);
    }
}
