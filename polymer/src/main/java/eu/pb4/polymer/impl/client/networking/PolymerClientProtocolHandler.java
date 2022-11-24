package eu.pb4.polymer.impl.client.networking;

import com.mojang.brigadier.StringReader;
import eu.pb4.polymer.api.client.*;
import eu.pb4.polymer.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.impl.networking.ClientPackets;
import eu.pb4.polymer.impl.networking.ServerPackets;
import eu.pb4.polymer.impl.networking.packets.*;
import eu.pb4.polymer.impl.other.EventRunners;
import eu.pb4.polymer.mixin.other.ItemGroupsAccessor;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolymerClientProtocolHandler {
    public static volatile int packetsPerSecond = 0;

    private static int currentPacketsPerSecond = 0;
    private static int ticker = 0;

    public static void tick() {
        ticker++;

        if (ticker >= 20) {
            ticker = 0;

            packetsPerSecond = currentPacketsPerSecond;
            currentPacketsPerSecond = 0;
        }

    }

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
                PolymerImpl.LOGGER.error(e.toString());
            }
            currentPacketsPerSecond++;
        }
    }

    private static boolean handle(ClientPlayNetworkHandler handler, String packet, int version, PacketByteBuf buf) {
        return switch (packet) {
            case ServerPackets.HANDSHAKE -> handleHandshake(handler, version, buf);
            case ServerPackets.DISABLE -> handleDisable();

            case ServerPackets.WORLD_SET_BLOCK_UPDATE -> handleSetBlock(handler, version, buf);
            case ServerPackets.WORLD_CHUNK_SECTION_UPDATE -> handleWorldSectionUpdate(handler, version, buf);
            case ServerPackets.WORLD_ENTITY -> handleEntity(handler, version, buf);

            case ServerPackets.SYNC_STARTED -> run(handler, () -> {
                PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN);
            });
            case ServerPackets.SYNC_INFO -> handleSyncInfo(handler, version, buf);
            case ServerPackets.SYNC_FINISHED -> run(handler, () -> {
                PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN);
            });
            case ServerPackets.SYNC_BLOCK -> handleGenericSync(handler, version, buf, PolymerBlockEntry::read,
                    (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual(), Registries.BLOCK.get(entry.identifier()))));
            case ServerPackets.SYNC_ITEM -> handleGenericSync(handler, version, buf, PolymerItemEntry::read,
                    (entry) -> {
                        var regEntry = Registries.ITEM.getEntry(RegistryKey.of(RegistryKeys.ITEM, entry.identifier()));

                        InternalClientRegistry.ITEMS.set(entry.identifier(), entry.numId(),
                                new ClientPolymerItem(
                                        entry.identifier(),
                                        entry.representation(),
                                        entry.foodLevels(),
                                        entry.saturation(),
                                        entry.miningTool(),
                                        entry.miningLevel(),
                                        entry.stackSize(),
                                        regEntry.isPresent() ? regEntry.get().value() : null
                                ));
                    });
            case ServerPackets.SYNC_BLOCKSTATE -> handleGenericSync(handler, version, buf, PolymerBlockStateEntry::read,
                        (entry) -> InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId()), blockStateOrNull(entry.states(), InternalClientRegistry.BLOCKS.get(entry.blockId()))), entry.numId()));
            case ServerPackets.SYNC_ENTITY -> handleGenericSync(handler, version, buf, PolymerEntityEntry::read,
                    (entry) -> InternalClientRegistry.ENTITY_TYPES.set(entry.identifier(), entry.rawId(), new ClientPolymerEntityType(entry.identifier(), entry.name(), Registries.ENTITY_TYPE.get(entry.identifier()))));
            case ServerPackets.SYNC_VILLAGER_PROFESSION -> handleGenericSync(handler, version, buf, IdValueEntry::read,
                    (entry) -> InternalClientRegistry.VILLAGER_PROFESSIONS.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), Registries.VILLAGER_PROFESSION.get(entry.id()))));
            case ServerPackets.SYNC_BLOCK_ENTITY -> handleGenericSync(handler, version, buf, IdValueEntry::read,
                    (entry) -> InternalClientRegistry.BLOCK_ENTITY.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), Registries.BLOCK_ENTITY_TYPE.get(entry.id()))));
            case ServerPackets.SYNC_STATUS_EFFECT -> handleGenericSync(handler, version, buf, IdValueEntry::read,
                    (entry) -> InternalClientRegistry.STATUS_EFFECT.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), Registries.STATUS_EFFECT.get(entry.id()))));
            case ServerPackets.SYNC_ENCHANTMENT -> handleGenericSync(handler, version, buf, IdValueEntry::read,
                    (entry) -> InternalClientRegistry.ENCHANTMENT.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), Registries.ENCHANTMENT.get(entry.id()))));

            case ServerPackets.SYNC_TAGS -> handleGenericSync(handler, version, buf, PolymerTagEntry::read, PolymerClientProtocolHandler::registerTag);
            case ServerPackets.SYNC_ITEM_GROUP_DEFINE -> handleItemGroupDefine(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_CONTENTS_ADD -> handleItemGroupContentsAdd(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR -> handleItemGroupContentsClear(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_REMOVE -> handleItemGroupRemove(handler, version, buf);
            case ServerPackets.SYNC_ITEM_GROUP_APPLY_UPDATE -> handleItemGroupApplyUpdates(handler, version, buf);
            case ServerPackets.SYNC_CLEAR -> run(handler, InternalClientRegistry::clear);
            case ServerPackets.DEBUG_VALIDATE_STATES -> handleGenericSync(handler, version, buf, DebugBlockStateEntry::read, PolymerClientProtocolHandler::handleDebugValidateStates);

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

    private static void registerTag(PolymerTagEntry tagEntry) {
            var reg = InternalClientRegistry.BY_VANILLA_ID.get(tagEntry.registry());
            if (reg != null) {
                for (var tag : tagEntry.tags()) {
                    reg.createTag(tag.id(), tag.ids());
                }
            }
    }

    private static void handleDebugValidateStates(DebugBlockStateEntry entry) {
        if (PolymerImpl.DEVELOPER_MODE) {
            var chat = MinecraftClient.getInstance().inGameHud.getChatHud();

            var state = Block.STATE_IDS.get(entry.numId());

            if (state == null) {
                chat.addMessage(Text.literal("Missing BlockState! | " + entry.numId() + " | Server: " + entry.asString()));
            } else {
                var debug = DebugBlockStateEntry.of(state, null, 0);

                if (!debug.equals(entry)) {
                    chat.addMessage(Text.literal("Mismatched BlockState! | " + entry.numId() + " | Server: " + entry.asString() + " | Client: " + debug.asString()));
                }
            }
        }
    }

    private static boolean handleDisable() {
        MinecraftClient.getInstance().execute(() -> {
            InternalClientRegistry.disable();
        });
        return true;
    }

    private static boolean handleSyncInfo(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        return true;
    }

    @Nullable
    private static BlockState blockStateOrNull(Map<String, String> states, ClientPolymerBlock clientPolymerBlock) {
        if (clientPolymerBlock.registryEntry() != null) {
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
                var parsed = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), new StringReader(path.toString()), false);

                return parsed.blockState();
            } catch (Exception e) {
                // noop
            }
        }

        return null;
    }

    private static boolean run(ClientPlayNetworkHandler handler, Runnable runnable) {
        MinecraftClient.getInstance().execute(runnable);
        return true;
    }

    private static boolean handleItemGroupApplyUpdates(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {

            MinecraftClient.getInstance().execute(() -> {
                if (ItemGroups.enabledFeatures != null) {
                    ItemGroupsAccessor.callUpdateEntries(ItemGroups.enabledFeatures, ItemGroups.operatorEnabled);
                }
                PolymerClientUtils.ON_SEARCH_REBUILD.invoke(EventRunners.RUN);
            });
            return true;
        }
        return false;
    }

    private static boolean handleItemGroupDefine(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            var id = buf.readIdentifier();
            var name = buf.readText();
            var icon = PolymerImplUtils.readStack(buf);

            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((t) -> t.getIdentifier().equals(id));
                InternalClientRegistry.createItemGroup(id, name, icon);
            });

            return true;
        }
        return false;
    }

    private static boolean handleItemGroupRemove(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            var id = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id));
            });
            return true;
        }

        return false;
    }

    private static boolean handleItemGroupContentsAdd(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            var id = buf.readIdentifier();

            var size = buf.readVarInt();

            var groupList = new ArrayList<ItemStack>();
            var searchList = new ArrayList<ItemStack>();

            for (int i = 0; i < size; i++) {
                var stack = PolymerImplUtils.readStack(buf);
                if (!stack.isEmpty()) {
                    stack = stack.copy();
                    stack.setCount(1);
                    groupList.add(stack);
                }
            }

            size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                var stack = PolymerImplUtils.readStack(buf);
                if (!stack.isEmpty()) {
                    stack = stack.copy();
                    stack.setCount(1);
                    searchList.add(stack);
                }
            }

            MinecraftClient.getInstance().execute(() -> {
                ItemGroup group = InternalClientRegistry.getItemGroup(id);

                if (group != null) {
                    var groupAccess = (ClientItemGroupExtension) group;

                    for (var stack : groupList) {
                        groupAccess.polymer$addStackGroup(stack);
                    }

                    for (var stack : searchList) {
                        groupAccess.polymer$addStackSearch(stack);
                    }
                }
            });
            return true;
        }
        return false;
    }

    private static boolean handleItemGroupContentsClear(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            var id = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                ItemGroup group = InternalClientRegistry.getItemGroup(id);

                if (group != null) {
                    var groupAccess = (ClientItemGroupExtension) group;
                    groupAccess.polymer$clearStacks();
                }

            });
            return true;
        }
        return false;
    }

    private static boolean handleEntity(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1) {
            var id = buf.readVarInt();
            var polymerId = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                var entity = handler.getWorld().getEntityById(id);
                if (entity != null) {
                    ((ClientEntityExtension) entity).polymer$setId(polymerId);
                }
            });
            return true;
        }
        return false;
    }

    private static boolean handleSetBlock(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 2) {
            var pos = buf.readBlockPos();
            var id = buf.readVarInt();
            var block = InternalClientRegistry.BLOCK_STATES.get(id);
            if (block != null) {
                MinecraftClient.getInstance().execute(() -> {
                    var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                            ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
                            ChunkStatus.FULL,
                            false
                    );

                    if (chunk != null) {
                        ((ClientBlockStorageInterface) chunk).polymer$setClientBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                        PolymerClientUtils.ON_BLOCK_UPDATE.invoke(c -> c.accept(pos, block));

                        if (block.blockState() != null && PolymerClientDecoded.checkDecode(block.blockState().getBlock())) {
                            chunk.setBlockState(pos, block.blockState(), false);
                        }
                    }
                });
            }
            return true;
        }
        return false;
    }

    private static boolean handleWorldSectionUpdate(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 2) {
            var sectionPos = buf.readChunkSectionPos();
            var size = buf.readVarInt();

            var blockPos = new short[size];
            var states = new ClientPolymerBlock.State[size];

            for (int i = 0; i < size; i++) {
                var value = buf.readVarLong();
                blockPos[i] = (short) ((int) (value & 4095L));
                states[i] = InternalClientRegistry.BLOCK_STATES.get((int) (value >>> 12));
            }

            MinecraftClient.getInstance().execute(() -> {
                var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                        sectionPos.getX(), sectionPos.getZ(),
                        ChunkStatus.FULL,
                        false
                );

                if (chunk != null) {
                    var section = chunk.getSection(chunk.sectionCoordToIndex(sectionPos.getY()));
                    if (section instanceof ClientBlockStorageInterface storage) {
                        var mutableBlockPos = new BlockPos.Mutable(0, 0, 0);
                        for (int i = 0; i < size; i++) {
                            var pos = blockPos[i];
                            var block = states[i];
                            if (block != null) {
                                var x = ChunkSectionPos.unpackLocalX(pos);
                                var y = ChunkSectionPos.unpackLocalY(pos);
                                var z = ChunkSectionPos.unpackLocalZ(pos);
                                mutableBlockPos.set(sectionPos.getMinX() + x, sectionPos.getMinX() + y, sectionPos.getMinX() + z);
                                PolymerClientUtils.ON_BLOCK_UPDATE.invoke(c -> c.accept(mutableBlockPos, block));
                                storage.polymer$setClientBlock(x, y, z, block);

                                if (block.blockState() != null && PolymerClientDecoded.checkDecode(block.blockState().getBlock())) {
                                    section.setBlockState(x, y, z, block.blockState());
                                }
                            }
                        }
                    }
                }
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
