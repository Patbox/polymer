package eu.pb4.polymer.core.impl.client.networking;

import com.mojang.brigadier.StringReader;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.core.api.client.*;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.impl.ClientMetadataKeys;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.interfaces.ClientBlockStorageInterface;
import eu.pb4.polymer.core.impl.client.interfaces.ClientEntityExtension;
import eu.pb4.polymer.core.impl.client.interfaces.ClientCreativeModeTabExtension;
import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.payloads.PolymerGenericListPayload;
import eu.pb4.polymer.core.impl.networking.payloads.PolymerNoOpPayload;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.core.impl.other.EventRunners;
import eu.pb4.polymer.core.impl.other.ImplPolymerRegistry;
import eu.pb4.polymer.core.mixin.other.CreativeModeTabsAccessor;
import eu.pb4.polymer.networking.api.client.PolymerClientNetworking;
import eu.pb4.polymer.networking.impl.NetImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

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
    private static long syncStarted = -1;

    public static void register() {
        registerCommonHandler(PolymerNoOpPayload.class, (client, handler, packet) -> {});
        registerPlayHandler(PolymerBlockUpdateS2CPayload.class, PolymerClientProtocolHandler::handleSetBlock);
        registerPlayHandler(PolymerSectionUpdateS2CPayload.class, PolymerClientProtocolHandler::handleWorldSectionUpdate);
        registerPlayHandler(PolymerEntityS2CPayload.class, PolymerClientProtocolHandler::handleEntity);

        registerCommonHandler(PolymerSyncStartedS2CPayload.class, (handler, version, buf) -> {
            syncStarted = System.currentTimeMillis();
            PolymerClientUtils.ON_SYNC_STARTED.invoker().run();
        });
        registerCommonHandler(PolymerSyncFinishedS2CPayload.class, (handler, version, buf) -> {
            if (PolymerImpl.LOG_SYNC_TIME_CLIENT) {
                PolymerImpl.LOGGER.info("Polymer Sync took {} ms", System.currentTimeMillis() - syncStarted);
            }
            InternalClientRegistry.updateBlockStatesPaletteProvider();

            PolymerClientUtils.ON_SYNC_FINISHED.invoker().run();
        });

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

        registerGenericListHandler(S2CPackets.SYNC_BLOCK, PolymerBlockEntry.class, (entry) -> InternalClientRegistry.BLOCKS.set(entry.identifier(), entry.numId(),
                new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.hardness(), switch (entry.miningDeltaLogic()) {
                    case DEFAULT -> ClientPolymerBlock.MiningDeltaLogic.DEFAULT;
                    case VANILLA -> ClientPolymerBlock.MiningDeltaLogic.VANILLA;
                    case CUSTOM_SERVER -> ClientPolymerBlock.MiningDeltaLogic.CUSTOM_SERVER;
                    case TOOL_REQUIRED -> ClientPolymerBlock.MiningDeltaLogic.TOOL_REQUIRED;
                }, entry.text(), entry.visual(), getNonDefault(BuiltInRegistries.BLOCK, entry.identifier()), entry.visualStack())));
        registerGenericListHandler(S2CPackets.SYNC_ITEM, PolymerItemEntry.class, (entry) -> {

                    InternalClientRegistry.ITEMS.set(entry.identifier(), entry.numId(),
                            new ClientPolymerItem(
                                    entry.identifier(),
                                    entry.representation(),
                                    getNonDefault(BuiltInRegistries.ITEM, entry.identifier())
                            ));
                });
        registerGenericListHandler(S2CPackets.SYNC_BLOCKSTATE, PolymerBlockStateEntry.class,
                (entry) -> InternalClientRegistry.BLOCK_STATES.addMapping(new ClientPolymerBlock.State(entry.properties(), InternalClientRegistry.BLOCKS.byId(entry.blockId()), blockStateOrNull(entry.properties(), InternalClientRegistry.BLOCKS.byId(entry.blockId()))), entry.numId()));

        registerGenericListHandler(S2CPackets.SYNC_ENTITY, PolymerEntityEntry.class,
                (entry) -> InternalClientRegistry.ENTITY_TYPES.set(entry.identifier(), entry.rawId(), new ClientPolymerEntityType(entry.identifier(), entry.name(), getNonDefault(BuiltInRegistries.ENTITY_TYPE, entry.identifier()))));

        registerGenericListHandler(S2CPackets.SYNC_VILLAGER_PROFESSION, InternalClientRegistry.VILLAGER_PROFESSIONS, BuiltInRegistries.VILLAGER_PROFESSION);
        registerGenericListHandler(S2CPackets.SYNC_BLOCK_ENTITY, InternalClientRegistry.BLOCK_ENTITY, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        registerGenericListHandler(S2CPackets.SYNC_STATUS_EFFECT, InternalClientRegistry.STATUS_EFFECT, BuiltInRegistries.MOB_EFFECT);
        registerGenericListHandler(S2CPackets.SYNC_FLUID, InternalClientRegistry.FLUID, BuiltInRegistries.FLUID);
        registerGenericListHandler(S2CPackets.SYNC_DATA_COMPONENT_TYPE, InternalClientRegistry.DATA_COMPONENT_TYPE, BuiltInRegistries.DATA_COMPONENT_TYPE);
        registerGenericListHandler(S2CPackets.SYNC_ENCHANTMENT_COMPONENT_TYPE, InternalClientRegistry.ENCHANTMENT_COMPONENT_TYPE, BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE);


        registerGenericListHandler(S2CPackets.SYNC_TAGS, PolymerTagEntry.class, PolymerClientProtocolHandler::registerTag);

        registerGenericListHandler(S2CPackets.DEBUG_VALIDATE_STATES, DebugBlockStateEntry.class, PolymerClientProtocolHandler::handleDebugValidateStates);



        PolymerClientNetworking.AFTER_METADATA_RECEIVED.register(() -> {
            InternalClientRegistry.setVersion(PolymerClientNetworking.getServerVersion(),
                    PolymerClientNetworking.getMetadata(ServerMetadataKeys.MINECRAFT_PROTOCOL, IntTag.TYPE));
            var limitedF3 = PolymerClientNetworking.getMetadata(ServerMetadataKeys.LIMITED_F3, ByteTag.TYPE);

            InternalClientRegistry.limitedF3 = limitedF3 != null && limitedF3.byteValue() != 0;
        });

        PolymerClientNetworking.AFTER_DISABLE.register(InternalClientRegistry::disable);

        PolymerClientNetworking.BEFORE_METADATA_SYNC.register(() -> {
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.ADVANCED_TOOLTIP, ByteTag.valueOf(Minecraft.getInstance().options.advancedItemTooltips));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.BLOCKSTATE_BITS, IntTag.valueOf(Mth.ceillog2(Block.BLOCK_STATE_REGISTRY.size())));
            PolymerClientNetworking.setClientMetadata(ClientMetadataKeys.MINECRAFT_PROTOCOL, IntTag.valueOf(SharedConstants.getProtocolVersion()));
        });
    }

    private static <T> T getNonDefault(DefaultedRegistry<T> registry, Identifier identifier) {
        return registry.containsKey(identifier) ? registry.getValue(identifier) : null;
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
            var chat = Minecraft.getInstance().gui.getChat();

            var state = Block.BLOCK_STATE_REGISTRY.byId(entry.numId());

            if (state == null) {
                chat.addClientSystemMessage(Component.literal("Missing BlockState! | " + entry.numId() + " | Server: " + entry.asString()));
            } else {
                var debug = DebugBlockStateEntry.of(state, null, 0);

                if (!debug.equals(entry)) {
                    chat.addClientSystemMessage(Component.literal("Mismatched BlockState! | " + entry.numId() + " | Server: " + entry.asString() + " | Client: " + debug.asString()));
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
                var parsed = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, new StringReader(path.toString()), false);

                return parsed.blockState();
            } catch (Exception e) {
                // noop
            }
        }

        return null;
    }

    private static void handleItemGroupApplyUpdates(Minecraft client, ClientCommonPacketListenerImpl handler, PolymerItemGroupApplyUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                if (CreativeModeTabsAccessor.getCACHED_PARAMETERS() != null) {
                    CreativeModeTabsAccessor.callBuildAllTabContents(CreativeModeTabsAccessor.getCACHED_PARAMETERS());
                }
                PolymerClientUtils.ON_SEARCH_REBUILD.invoker().run();
            });
        }
    }

    private static void handleItemGroupDefine(Minecraft client, ClientCommonPacketListenerImpl handler, PolymerItemGroupDefineS2CPayload payload) {
        if ( InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((t) -> t.getIdentifier().equals(payload.groupId()));
                InternalClientRegistry.createItemGroup(payload.groupId(), payload.name(), payload.icon());
            });

        }
    }

    private static void handleItemGroupRemove(Minecraft client, ClientCommonPacketListenerImpl handler, PolymerItemGroupRemoveS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                InternalClientRegistry.clearTabs((x) -> x.getIdentifier().equals(payload.groupId()));
            });
        }

    }

    private static void handleItemGroupContentsAdd(Minecraft client, ClientCommonPacketListenerImpl handler, PolymerItemGroupContentAddS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                CreativeModeTab group = InternalClientRegistry.getItemGroup(payload.groupId());

                if (group != null) {
                    var groupAccess = (ClientCreativeModeTabExtension) group;

                    groupAccess.polymer$handleEntries(payload.stacksMain(), payload.stacksSearch());
                }
            });
        }
    }

    private static void handleItemGroupContentsClear(Minecraft client, ClientCommonPacketListenerImpl handler, PolymerItemGroupContentClearS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                CreativeModeTab group = InternalClientRegistry.getItemGroup(payload.groupId());

                if (group != null) {
                    var groupAccess = (ClientCreativeModeTabExtension) group;
                    groupAccess.polymer$clearStacks();
                }

            });
        }
    }

    private static void handleEntity(Minecraft client, ClientPacketListener handler, PolymerEntityS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                var entity = handler.getLevel().getEntity(payload.entityId());
                if (entity != null) {
                    ((ClientEntityExtension) entity).polymer$setId(payload.typeId());
                }
            });
        }
    }

    private static void handleSetBlock(Minecraft client, ClientPacketListener handler, PolymerBlockUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            Minecraft.getInstance().execute(() -> {
                var block = InternalClientRegistry.BLOCK_STATES.byId(payload.blockId());
                if (block != null) {
                    var pos = payload.pos();
                    var chunk = Minecraft.getInstance().level.getChunkSource().getChunk(
                            SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()),
                            ChunkStatus.FULL,
                            false
                    );

                    if (chunk != null) {
                        ((ClientBlockStorageInterface) chunk).polymer$setClientBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                        PolymerClientUtils.ON_BLOCK_UPDATE.invoker().accept(pos, block);

                        if (block.blockState() != null && PolymerClientDecoded.checkDecode(block.blockState().getBlock())) {
                            chunk.setBlockState(pos, block.blockState());
                        }
                    }
                }
            });

        }
    }

    private static void handleWorldSectionUpdate(Minecraft client, ClientPacketListener handler, PolymerSectionUpdateS2CPayload payload) {
        if (InternalClientRegistry.enabled) {
            var sectionPos = payload.chunkPos();

            Minecraft.getInstance().execute(() -> {
                var chunk = Minecraft.getInstance().level.getChunkSource().getChunk(
                        sectionPos.getX(), sectionPos.getZ(),
                        ChunkStatus.FULL,
                        false
                );
                var blockPos = payload.pos();
                var states = payload.blocks();

                if (chunk != null) {
                    var section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionPos.getY()));
                    if (section instanceof ClientBlockStorageInterface storage) {
                        var mutableBlockPos = new BlockPos.MutableBlockPos(0, 0, 0);
                        for (int i = 0; i < states.length; i++) {
                            var pos = blockPos[i];
                            var block = InternalClientRegistry.BLOCK_STATES.byId(states[i]);
                            if (block != null) {
                                var x = SectionPos.sectionRelativeX(pos);
                                var y = SectionPos.sectionRelativeY(pos);
                                var z = SectionPos.sectionRelativeZ(pos);
                                mutableBlockPos.set(sectionPos.minBlockX() + x, sectionPos.minBlockX() + y, sectionPos.minBlockX() + z);
                                PolymerClientUtils.ON_BLOCK_UPDATE.invoker().accept(mutableBlockPos, block);
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


    private static <T> void handleGenericList(Minecraft client, ClientCommonPacketListenerImpl handle, PolymerGenericListPayload<?> payload) {
        if (!InternalClientRegistry.enabled) {
            return;
        }

        //noinspection unchecked
        var consumer = (Consumer<Object>) GENERIC_LIST_HANDLERS.get(payload.id().id());

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
        T read(FriendlyByteBuf buf, int version);
    }
}
