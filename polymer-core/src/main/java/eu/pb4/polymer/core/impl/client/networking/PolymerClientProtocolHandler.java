package eu.pb4.polymer.core.impl.client.networking;

import com.mojang.brigadier.StringReader;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.networking.ServerPackets;
import eu.pb4.polymer.core.impl.networking.packets.*;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.other.ItemGroupsAccessor;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Consumer;

import static eu.pb4.polymer.networking.api.client.PolymerClientNetworking.registerPacketHandler;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolymerClientProtocolHandler {

    public static void register() {
        registerPacketHandler(ServerPackets.WORLD_SET_BLOCK_UPDATE, PolymerClientProtocolHandler::handleSetBlock);
        registerPacketHandler(ServerPackets.WORLD_CHUNK_SECTION_UPDATE, PolymerClientProtocolHandler::handleWorldSectionUpdate);
        registerPacketHandler(ServerPackets.WORLD_ENTITY, PolymerClientProtocolHandler::handleEntity);

        registerPacketHandler(ServerPackets.SYNC_STARTED, (handler, version, buf) -> PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN));
        registerPacketHandler(ServerPackets.SYNC_INFO, PolymerClientProtocolHandler::handleSyncInfo);
        registerPacketHandler(ServerPackets.SYNC_FINISHED, (handler, version, buf) -> PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN));
        registerPacketHandler(ServerPackets.SYNC_BLOCK, (handler, version, buf) -> handleGenericSync(handler, version, buf, PolymerBlockEntry::read,
                (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual(), Registries.BLOCK.get(entry.identifier())))));
        registerPacketHandler(ServerPackets.SYNC_ITEM, (handler, version, buf) -> handleGenericSync(handler, version, buf, PolymerItemEntry::read,
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
                }));
        registerPacketHandler(ServerPackets.SYNC_BLOCKSTATE, (handler, version, buf) -> handleGenericSync(handler, version, buf, PolymerBlockStateEntry::read,
                (entry) -> InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.properties(), InternalClientRegistry.BLOCKS.get(entry.blockId()), blockStateOrNull(entry.properties(), InternalClientRegistry.BLOCKS.get(entry.blockId()))), entry.numId())));

        registerPacketHandler(ServerPackets.SYNC_ENTITY, (handler, version, buf) -> handleGenericSync(handler, version, buf, PolymerEntityEntry::read,
                (entry) -> InternalClientRegistry.ENTITY_TYPES.set(entry.identifier(), entry.rawId(), new ClientPolymerEntityType(entry.identifier(), entry.name(), Registries.ENTITY_TYPE.get(entry.identifier())))));

        registerGenericHandler(ServerPackets.SYNC_VILLAGER_PROFESSION, InternalClientRegistry.VILLAGER_PROFESSIONS, Registries.VILLAGER_PROFESSION);
        registerGenericHandler(ServerPackets.SYNC_BLOCK_ENTITY, InternalClientRegistry.BLOCK_ENTITY, Registries.BLOCK_ENTITY_TYPE);
        registerGenericHandler(ServerPackets.SYNC_STATUS_EFFECT, InternalClientRegistry.STATUS_EFFECT, Registries.STATUS_EFFECT);
        registerGenericHandler(ServerPackets.SYNC_ENCHANTMENT, InternalClientRegistry.ENCHANTMENT, Registries.ENCHANTMENT);
        registerGenericHandler(ServerPackets.SYNC_FLUID, InternalClientRegistry.FLUID, Registries.FLUID);

        registerPacketHandler(ServerPackets.SYNC_TAGS, (handler, version, buf) -> handleGenericSync(handler, version, buf, PolymerTagEntry::read, PolymerClientProtocolHandler::registerTag));
        registerPacketHandler(ServerPackets.SYNC_ITEM_GROUP_DEFINE, PolymerClientProtocolHandler::handleItemGroupDefine);
        registerPacketHandler(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_ADD, PolymerClientProtocolHandler::handleItemGroupContentsAdd);
        registerPacketHandler(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR, PolymerClientProtocolHandler::handleItemGroupContentsClear);
        registerPacketHandler(ServerPackets.SYNC_ITEM_GROUP_REMOVE, PolymerClientProtocolHandler::handleItemGroupRemove);
        registerPacketHandler(ServerPackets.SYNC_ITEM_GROUP_APPLY_UPDATE, PolymerClientProtocolHandler::handleItemGroupApplyUpdates);
        registerPacketHandler(ServerPackets.SYNC_CLEAR, (handler, version, buf) -> {
            InternalClientRegistry.clear();
        });
        registerPacketHandler(ServerPackets.DEBUG_VALIDATE_STATES, (handler, version, buf) -> handleGenericSync(handler, version, buf, DebugBlockStateEntry::read, PolymerClientProtocolHandler::handleDebugValidateStates));


        PolymerClientNetworking.AFTER_METADATA_RECEIVED.register(() -> {
            InternalClientRegistry.setVersion(PolymerClientNetworking.getServerVersion(),
                    PolymerClientNetworking.getMetadata(ServerMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE));
        });

        PolymerClientNetworking.AFTER_DISABLE.register(InternalClientRegistry::disable);

        PolymerClientNetworking.BEFORE_METADATA_SYNC.register(() -> {
            var lang = MinecraftClient.getInstance().getLanguageManager().getLanguage();
            if (lang == null) {
                lang = "en_us";
            }

            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.BLOCKSTATE_BITS, NbtInt.of(MathHelper.ceilLog2(Block.STATE_IDS.size())));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.of(SharedConstants.getProtocolVersion()));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.LANGUAGE, NbtString.of(lang));
        });
    }

    private static <T> void registerGenericHandler(Identifier identifier, ImplPolymerRegistry<ClientPolymerEntry<T>> polymerRegistry, Registry<T> registry) {
        registerPacketHandler(identifier, (handler, version, buf) -> handleGenericSync(handler, version, buf, IdValueEntry::read,
                (entry) -> polymerRegistry.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), registry.get(entry.id())))));
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
        if (CommonImpl.DEVELOPER_MODE) {
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

    private static void handleSyncInfo(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
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

    private static void handleItemGroupApplyUpdates(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                if (ItemGroupsAccessor.getDisplayContext() != null) {
                    ItemGroupsAccessor.callUpdateEntries(ItemGroupsAccessor.getDisplayContext());
                }
                PolymerClientUtils.ON_SEARCH_REBUILD.invoke(EventRunners.RUN);
            });
        }
    }

    private static void handleItemGroupDefine(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
            var id = buf.readIdentifier();
            var name = buf.readText();
            var icon = PolymerImplUtils.readStack(buf);

            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((t) -> t.getIdentifier().equals(id));
                InternalClientRegistry.createItemGroup(id, name, icon);
            });

        }
    }

    private static void handleItemGroupRemove(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
            var id = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(id));
            });
        }

    }

    private static void handleItemGroupContentsAdd(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
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
        }
    }

    private static void handleItemGroupContentsClear(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
            var id = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                ItemGroup group = InternalClientRegistry.getItemGroup(id);

                if (group != null) {
                    var groupAccess = (ClientItemGroupExtension) group;
                    groupAccess.polymer$clearStacks();
                }

            });
        }
    }

    private static void handleEntity(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version > -1 && InternalClientRegistry.enabled) {
            var id = buf.readVarInt();
            var polymerId = buf.readIdentifier();
            MinecraftClient.getInstance().execute(() -> {
                var entity = handler.getWorld().getEntityById(id);
                if (entity != null) {
                    ((ClientEntityExtension) entity).polymer$setId(polymerId);
                }
            });
        }
    }

    private static void handleSetBlock(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 2 && InternalClientRegistry.enabled) {
            var pos = buf.readBlockPos();
            var id = buf.readVarInt();
            MinecraftClient.getInstance().execute(() -> {
                var block = InternalClientRegistry.BLOCK_STATES.get(id);
                if (block != null) {
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
                }
            });

        }
    }

    private static void handleWorldSectionUpdate(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf) {
        if (version == 2 && InternalClientRegistry.enabled) {
            var sectionPos = buf.readChunkSectionPos();
            var size = buf.readVarInt();

            var blockPos = new short[size];
            var states = new int[size];

            for (int i = 0; i < size; i++) {
                var value = buf.readVarLong();
                blockPos[i] = (short) ((int) (value & 4095L));
                states[i] = (int) (value >>> 12);
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
                            var block = InternalClientRegistry.BLOCK_STATES.get(states[i]);
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

        }
    }


    private static <T> void handleGenericSync(ClientPlayNetworkHandler handler, int version, PacketByteBuf buf, EntryReader<T> reader, Consumer<T> entryConsumer) {
        if (!InternalClientRegistry.enabled) {
            return;
        }
        var size = buf.readVarInt();

        var list = new ArrayList<T>();
        for (int i = 0; i < size; i++) {
            var entry = reader.read(buf, version);
            if (entry != null) {
                list.add(entry);
            }
        }

        try {
            for (var entry : list) {
                entryConsumer.accept(entry);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    interface EntryReader<T> {
        @Nullable
        T read(PacketByteBuf buf, int version);
    }
}
