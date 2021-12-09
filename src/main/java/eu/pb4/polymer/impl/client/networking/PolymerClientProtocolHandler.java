package eu.pb4.polymer.impl.client.networking;

import com.mojang.brigadier.StringReader;
import eu.pb4.polymer.api.client.PolymerClientPacketHandler;
import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.api.client.registry.ClientPolymerBlock;
import eu.pb4.polymer.api.client.registry.ClientPolymerEntityType;
import eu.pb4.polymer.api.client.registry.ClientPolymerItem;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.impl.PolymerImpl;
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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.search.SearchManager;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolymerClientProtocolHandler {
    public static final HashMap<String, PolymerClientPacketHandler> CUSTOM_PACKETS = new HashMap<>();

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

            case ServerPackets.SYNC_STARTED -> run(() -> {
                InternalClientRegistry.STABLE = false;
                PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN);
            });
            case ServerPackets.SYNC_FINISHED -> run(() -> {
                InternalClientRegistry.STABLE = true;
                PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN);
            });
            case ServerPackets.SYNC_BLOCK -> handleGenericSync(handler, version, buf, PolymerBlockEntry::read,
                    (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual(), Registry.BLOCK.get(entry.identifier()))));
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
                    (entry) -> InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId()), blockStateOrNull(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId()))), entry.numId()));
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
            default -> {
                var packetHandler = CUSTOM_PACKETS.get(packet);
                if (packetHandler != null) {
                    packetHandler.onPacket(handler, version, buf);
                    yield true;
                }
                yield false;
            }
        };
    }

    @Nullable
    private static BlockState blockStateOrNull(Map<String, String> states, ClientPolymerBlock clientPolymerBlock) {
        if (clientPolymerBlock.realServerBlock() != null) {
            var path = new StringBuilder(clientPolymerBlock.identifier().toString());

            if (!states.isEmpty()) {
                path.append("[");
                var iterator = states.entrySet().iterator();
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    path.append(entry.getKey()).append("=").append(entry.getValue());

                    if (iterator.hasNext()) {
                        path.append(",");
                    }
                }
                path.append("]");
            }

            try {
                var parsed = new BlockArgumentParser(new StringReader(path.toString()), false).parse(true);

                return parsed.getBlockState();
            } catch (Exception e) {
                // noop
            }
        }

        return null;
    }

    private static boolean run(Runnable runnable) {
        runnable.run();
        return true;
    }

    private static boolean handleItemGroupRemove(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var id = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id)));
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
            MinecraftClient.getInstance().execute(() -> {
                var entity = handler.getWorld().getEntityById(id);
                if (entity != null) {
                    ((ClientEntityExtension) entity).polymer_setId(polymerId);
                }
            });
            return true;
        }
        return false;
    }

    private static boolean handleSetBlock(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var pos = buf.readBlockPos();
            var id = buf.readVarInt();
            MinecraftClient.getInstance().execute(() -> {
                var block = InternalClientRegistry.BLOCK_STATES.get(id);

                var chunk = handler.getWorld().getChunk(pos);

                if (block != null && chunk instanceof ClientBlockStorageInterface storage) {
                    storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                    PolymerClientUtils.ON_BLOCK_UPDATE.invoke(c -> c.accept(pos, block));
                }
            });
            return true;
        }
        return false;
    }

    private static boolean handleWorldSectionUpdate(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            var sectionPos = buf.readChunkSectionPos();
            var size = buf.readVarInt();

            var values = new long[size];

            for (int i = 0; i < size; i++) {
                values[i] = buf.readVarLong();
            }

            MinecraftClient.getInstance().execute(() -> {
                var chunk = handler.getWorld().getChunk(sectionPos.getX(), sectionPos.getZ());
                var section = chunk.getSection(chunk.sectionCoordToIndex(sectionPos.getY()));
                var blockPos = new BlockPos.Mutable(0, 0, 0);
                if (section instanceof ClientBlockStorageInterface storage) {
                    for (var value : values) {
                        var pos = (short) ((int) (value & 4095L));

                        var block = InternalClientRegistry.BLOCK_STATES.get((int) (value >>> 12));
                        if (block != null) {
                            var x = ChunkSectionPos.unpackLocalX(pos);
                            var y = ChunkSectionPos.unpackLocalY(pos);
                            var z = ChunkSectionPos.unpackLocalZ(pos);
                            blockPos.set(sectionPos.getMinX() + x, sectionPos.getMinX() + y, sectionPos.getMinX() + z);
                            PolymerClientUtils.ON_BLOCK_UPDATE.invoke(c -> c.accept(blockPos, block));
                            storage.polymer_setClientPolymerBlock(x, y, z, block);
                        }
                    }
                }
            });
            return true;

        }
        return false;
    }

    private static boolean handleSearchRebuild(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 0) {
            MinecraftClient.getInstance().execute(() -> {
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
            });
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

            MinecraftClient.getInstance().execute(() -> {
                var array = ItemGroupAccessor.getGROUPS();

                var newArray = new ItemGroup[array.length + 1];

                if (array.length >= 0) {
                    System.arraycopy(array, 0, newArray, 0, array.length);
                }

                ItemGroupAccessor.setGROUPS(newArray);

                var group = new InternalClientItemGroup(array.length, id, id.toString(), name, icon, stacks);
                InternalClientRegistry.ITEM_GROUPS.set(id, group);
            });

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

            MinecraftClient.getInstance().execute(() -> {
                PolymerClientUtils.ON_HANDSHAKE.invoke(EventRunners.RUN);
                PolymerClientProtocol.sendTooltipContext(handler);
                PolymerClientProtocol.sendSyncRequest(handler);
            });

            return true;
        }
        return false;
    }


    private static <T> boolean handleGenericSync(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf, EntryReader<T> reader, Consumer<T> entryConsumer) {
        var size = buf.readVarInt();

        var list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            var entry = reader.read(buf, version);
            if (entry != null) {
                list.add(entry);
            }
        }

        MinecraftClient.getInstance().execute(() -> {
            for (var entry : list) {
                entryConsumer.accept(entry);
            }
        });
        return true;
    }

    interface EntryReader<T> {
        @Nullable
        T read(PacketByteBuf buf, int version);
    }
}
