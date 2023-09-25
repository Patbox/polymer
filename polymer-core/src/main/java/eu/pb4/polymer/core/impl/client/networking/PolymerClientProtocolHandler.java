package eu.pb4.polymer.core.impl.client.networking;

import com.mojang.brigadier.StringReader;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.core.impl.client.interfaces.ClientItemGroupExtension;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.entry.DebugBlockStateEntry;
import eu.pb4.polymer.core.impl.networking.payloads.PolymerGenericListPayload;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.other.ItemGroupsAccessor;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.impl.NetImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.item.ItemGroup;
import net.minecraft.nbt.NbtByte;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static eu.pb4.polymer.networking.api.client.PolymerClientNetworking.registerCommonHandler;
import static eu.pb4.polymer.networking.api.client.PolymerClientNetworking.registerPlayHandler;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
@Environment(EnvType.CLIENT)
public class PolymerClientProtocolHandler {
    public static final Map<Identifier, Consumer<?>> GENERIC_LIST_HANDLERS = new HashMap<>();

    public static void register() {
        registerPlayHandler(PolymerBlockUpdateS2CPayload.class, PolymerClientProtocolHandler::handleSetBlock);
        registerPlayHandler(PolymerSectionUpdateS2CPayload.class, PolymerClientProtocolHandler::handleWorldSectionUpdate);
        registerPlayHandler(PolymerEntityS2CPayload.class, PolymerClientProtocolHandler::handleEntity);

        registerCommonHandler(PolymerSyncStartedS2CPayload.class, (handler, version, buf) -> PolymerClientUtils.ON_SYNC_STARTED.invoke(EventRunners.RUN));
        registerCommonHandler(PolymerSyncFinishedS2CPayload.class, (handler, version, buf) -> PolymerClientUtils.ON_SYNC_FINISHED.invoke(EventRunners.RUN));

        registerCommonHandler(PolymerItemGroupDefineS2CPayload.class, PolymerClientProtocolHandler::handleItemGroupDefine);
        registerCommonHandler(PolymerItemGroupContentAddS2CPayload.class, PolymerClientProtocolHandler::handleItemGroupContentsAdd);
        registerCommonHandler(PolymerItemGroupContentClearS2CPayload.class, PolymerClientProtocolHandler::handleItemGroupContentsClear);
        registerCommonHandler(PolymerItemGroupRemoveS2CPayload.class, PolymerClientProtocolHandler::handleItemGroupRemove);
        registerCommonHandler(PolymerItemGroupApplyUpdateS2CPayload.class, PolymerClientProtocolHandler::handleItemGroupApplyUpdates);
        registerCommonHandler(PolymerSyncClearS2CPayload.class, (client, handler, payload) -> {
            InternalClientRegistry.clear();
        });

        registerCommonHandler(PolymerSyncClearS2CPayload.class, (client, handler, payload) -> {
            InternalClientRegistry.clear();
        });

        registerCommonHandler(PolymerGenericListPayload.class, PolymerClientProtocolHandler::handleGenericList);

        registerGenericListHandler(S2CPackets.SYNC_BLOCK, PolymerBlockEntry.class, (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(), new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual(), Registries.BLOCK.get(entry.identifier()))));
        registerGenericListHandler(S2CPackets.SYNC_ITEM, PolymerItemEntry.class, (entry) -> {
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
        registerGenericListHandler(S2CPackets.SYNC_BLOCKSTATE, PolymerBlockStateEntry.class,
                (entry) -> InternalClientRegistry.BLOCK_STATES.set(new ClientPolymerBlock.State(entry.properties(), InternalClientRegistry.BLOCKS.get(entry.blockId()), blockStateOrNull(entry.properties(), InternalClientRegistry.BLOCKS.get(entry.blockId()))), entry.numId()));

        registerGenericListHandler(S2CPackets.SYNC_ENTITY, PolymerEntityEntry.class,
                (entry) -> InternalClientRegistry.ENTITY_TYPES.set(entry.identifier(), entry.rawId(), new ClientPolymerEntityType(entry.identifier(), entry.name(), Registries.ENTITY_TYPE.get(entry.identifier()))));

        registerGenericListHandler(S2CPackets.SYNC_VILLAGER_PROFESSION, InternalClientRegistry.VILLAGER_PROFESSIONS, Registries.VILLAGER_PROFESSION);
        registerGenericListHandler(S2CPackets.SYNC_BLOCK_ENTITY, InternalClientRegistry.BLOCK_ENTITY, Registries.BLOCK_ENTITY_TYPE);
        registerGenericListHandler(S2CPackets.SYNC_STATUS_EFFECT, InternalClientRegistry.STATUS_EFFECT, Registries.STATUS_EFFECT);
        registerGenericListHandler(S2CPackets.SYNC_ENCHANTMENT, InternalClientRegistry.ENCHANTMENT, Registries.ENCHANTMENT);
        registerGenericListHandler(S2CPackets.SYNC_FLUID, InternalClientRegistry.FLUID, Registries.FLUID);


        registerGenericListHandler(S2CPackets.SYNC_TAGS, PolymerTagEntry.class, PolymerClientProtocolHandler::registerTag);

        registerGenericListHandler(S2CPackets.DEBUG_VALIDATE_STATES, DebugBlockStateEntry.class, PolymerClientProtocolHandler::handleDebugValidateStates);



        PolymerClientNetworking.AFTER_METADATA_RECEIVED.register(() -> {
            InternalClientRegistry.setVersion(PolymerClientNetworking.getServerVersion(),
                    PolymerClientNetworking.getMetadata(ServerMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE));
            var limitedF3 = PolymerClientNetworking.getMetadata(ServerMetadataKeys.LIMITED_F3, NbtByte.TYPE);

            InternalClientRegistry.limitedF3 = limitedF3 != null && limitedF3.byteValue() != 0;
        });

        PolymerClientNetworking.AFTER_DISABLE.register(InternalClientRegistry::disable);

        PolymerClientNetworking.BEFORE_METADATA_SYNC.register(() -> {
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.ADVANCED_TOOLTIP, NbtByte.of(MinecraftClient.getInstance().options.advancedItemTooltips));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.BLOCKSTATE_BITS, NbtInt.of(MathHelper.ceilLog2(Block.STATE_IDS.size())));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.of(SharedConstants.getProtocolVersion()));
        });
    }

    private static <T> void registerGenericListHandler(Identifier id, Class<T> targetClass, Consumer<T> consumer) {
        GENERIC_LIST_HANDLERS.put(id, consumer);
    }

    private static <T> void registerGenericListHandler(Identifier id, ImplPolymerRegistry<ClientPolymerEntry<T>> polymerRegistry, Registry<T> vanillaRegistry) {
        registerGenericListHandler(id, IdValueEntry.class, (entry) -> polymerRegistry.set(entry.id(), entry.rawId(), ClientPolymerEntry.of(entry.id(), vanillaRegistry)));
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

    private static void handleItemGroupApplyUpdates(MinecraftClient client, ClientCommonNetworkHandler handler, PolymerItemGroupApplyUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                if (ItemGroupsAccessor.getDisplayContext() != null) {
                    ItemGroupsAccessor.callUpdateEntries(ItemGroupsAccessor.getDisplayContext());
                }
                PolymerClientUtils.ON_SEARCH_REBUILD.invoke(EventRunners.RUN);
            });
        }
    }

    private static void handleItemGroupDefine(MinecraftClient client, ClientCommonNetworkHandler handler, PolymerItemGroupDefineS2CPayload payload) {
        if ( InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((t) -> t.getIdentifier().equals(payload.groupId()));
                InternalClientRegistry.createItemGroup(payload.groupId(), payload.name(), payload.icon());
            });

        }
    }

    private static void handleItemGroupRemove(MinecraftClient client, ClientCommonNetworkHandler handler, PolymerItemGroupRemoveS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(payload.groupId()));
            });
        }

    }

    private static void handleItemGroupContentsAdd(MinecraftClient client, ClientCommonNetworkHandler handler, PolymerItemGroupContentAddS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                ItemGroup group = InternalClientRegistry.getItemGroup(payload.groupId());

                if (group != null) {
                    var groupAccess = (ClientItemGroupExtension) group;

                    for (var stack : payload.stacksMain()) {
                        groupAccess.polymer$addStackGroup(stack);
                    }

                    for (var stack : payload.stacksSearch()) {
                        groupAccess.polymer$addStackSearch(stack);
                    }
                }
            });
        }
    }

    private static void handleItemGroupContentsClear(MinecraftClient client, ClientCommonNetworkHandler handler, PolymerItemGroupContentClearS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                ItemGroup group = InternalClientRegistry.getItemGroup(payload.groupId());

                if (group != null) {
                    var groupAccess = (ClientItemGroupExtension) group;
                    groupAccess.polymer$clearStacks();
                }

            });
        }
    }

    private static void handleEntity(MinecraftClient client, ClientPlayNetworkHandler handler, PolymerEntityS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                var entity = handler.getWorld().getEntityById(payload.entityId());
                if (entity != null) {
                    ((ClientEntityExtension) entity).polymer$setId(payload.typeId());
                }
            });
        }
    }

    private static void handleSetBlock(MinecraftClient client, ClientPlayNetworkHandler handler, PolymerBlockUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            MinecraftClient.getInstance().execute(() -> {
                var block = InternalClientRegistry.BLOCK_STATES.get(payload.blockId());
                if (block != null) {
                    var pos = payload.pos();
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

    private static void handleWorldSectionUpdate(MinecraftClient client, ClientPlayNetworkHandler handler, PolymerSectionUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            var sectionPos = payload.chunkPos();

            MinecraftClient.getInstance().execute(() -> {
                var chunk = MinecraftClient.getInstance().world.getChunkManager().getChunk(
                        sectionPos.getX(), sectionPos.getZ(),
                        ChunkStatus.FULL,
                        false
                );
                var blockPos = payload.pos();
                var states = payload.blocks();

                if (chunk != null) {
                    var section = chunk.getSection(chunk.sectionCoordToIndex(sectionPos.getY()));
                    if (section instanceof ClientBlockStorageInterface storage) {
                        var mutableBlockPos = new BlockPos.Mutable(0, 0, 0);
                        for (int i = 0; i < states.length; i++) {
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


    private static <T> void handleGenericList(MinecraftClient client, ClientCommonNetworkHandler handle, PolymerGenericListPayload<?> payload) {
        if (!InternalClientRegistry.enabled) {
            return;
        }

        //noinspection unchecked
        var consumer = (Consumer<Object>) GENERIC_LIST_HANDLERS.get(payload.id());

        if (consumer != null) {
            try {
                for (var entry : payload.entries()) {
                    consumer.accept(entry);
                }
            } catch (Throwable e) {
                NetImpl.LOGGER.error("Handing of packet '" + payload.id() +"' failed!", e);
            }
        }
    }

    interface EntryReader<T> {
        @Nullable
        T read(PacketByteBuf buf, int version);
    }
}
