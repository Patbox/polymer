package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.payloads.*;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import org.jetbrains.annotations.ApiStatus;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

@ApiStatus.Internal
public class PolymerServerProtocol {
    public static void sendBlockUpdate(ServerGamePacketListenerImpl player, BlockPos pos, BlockState state) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_SET_BLOCK_UPDATE);

        if (PolymerImplUtils.POLYMER_STATES.contains(state) && version > -1) {
            player.send(new ClientboundCustomPayloadPacket(new PolymerBlockUpdateS2CPayload(pos, Block.BLOCK_STATE_REGISTRY.getId(state))));
        }
    }

    public static void sendMultiBlockUpdate(ServerGamePacketListenerImpl player, SectionPos chunkPos, short[] positions, BlockState[] blockStates) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var blocks = new IntArrayList();
            var pos = new ShortArrayList();

            for (int i = 0; i < blockStates.length; i++) {
                if (PolymerImplUtils.POLYMER_STATES.contains(blockStates[i])) {
                    blocks.add(Block.BLOCK_STATE_REGISTRY.getId(blockStates[i]));
                    pos.add(positions[i]);
                }
            }

            if (!blocks.isEmpty()) {
                player.send(new ClientboundCustomPayloadPacket(new PolymerSectionUpdateS2CPayload(chunkPos, pos.toShortArray(), blocks.toIntArray())));
            }
        }
    }

    public static void sendSectionUpdate(ServerGamePacketListenerImpl player, LevelChunk chunk) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var wci = (PolymerBlockPosStorage) chunk;
            if (wci.polymer$hasAny()) {
                var sections = chunk.getSections();
                for (var i = 0; i < sections.length; i++) {
                    var section = sections[i];
                    var storage = (PolymerBlockPosStorage) section;

                    if (section != null && storage.polymer$hasAny()) {
                        var set = storage.polymer$getBackendSet();
                        var blocks = new IntArrayList();


                        assert set != null;
                        for (var pos : set) {
                            int x = SectionPos.sectionRelativeX(pos);
                            int y = SectionPos.sectionRelativeY(pos);
                            int z = SectionPos.sectionRelativeZ(pos);
                            var state = section.getBlockState(x, y, z);
                            blocks.add(Block.BLOCK_STATE_REGISTRY.getId(state));
                        }

                        player.send(new ClientboundCustomPayloadPacket(new PolymerSectionUpdateS2CPayload(SectionPos.of(chunk.getPos(), chunk.getSectionYFromSectionIndex(i)),
                                set.toShortArray(), blocks.toIntArray())));
                    }
                }
            }
        }
    }


    public static void sendSyncPackets(ServerGamePacketListenerImpl handler, boolean fullSync) {
        if (PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_STARTED) == -1) {
            return;
        }

        var startTime = System.nanoTime();
        int version;

        handler.send(new ClientboundCustomPayloadPacket(new PolymerSyncStartedS2CPayload()));
        PolymerSyncUtils.ON_SYNC_STARTED.invoke((c) -> c.accept(handler));

        version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_CLEAR);
        if (version != -1) {
            handler.send(new ClientboundCustomPayloadPacket(new PolymerSyncClearS2CPayload()));
        }

        PolymerSyncUtils.BEFORE_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_ITEM_ID, getServerSideEntries(BuiltInRegistries.ITEM), false, PolymerItemEntry::of);
        PolymerSyncUtils.AFTER_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        if (fullSync) {
            PolymerSyncUtils.BEFORE_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));

            sendCreativeSyncPackets(handler);

            PolymerSyncUtils.AFTER_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));
        }

        PolymerSyncUtils.BEFORE_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_BLOCK_ID, getServerSideEntries(BuiltInRegistries.BLOCK), false, PolymerBlockEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        PolymerSyncUtils.BEFORE_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_BLOCKSTATE_ID, getServerSideEntries(Block.BLOCK_STATE_REGISTRY), false, PolymerBlockStateEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        PolymerSyncUtils.BEFORE_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_ENTITY_ID, getServerSideEntries(BuiltInRegistries.ENTITY_TYPE), false, PolymerEntityEntry::of);
        PolymerSyncUtils.AFTER_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        sendSync(handler, S2CPackets.SYNC_VILLAGER_PROFESSION_ID, BuiltInRegistries.VILLAGER_PROFESSION);
        sendSync(handler, S2CPackets.SYNC_STATUS_EFFECT_ID, BuiltInRegistries.MOB_EFFECT);
        sendSync(handler, S2CPackets.SYNC_BLOCK_ENTITY_ID, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        sendSync(handler, S2CPackets.SYNC_FLUID_ID, BuiltInRegistries.FLUID);
        sendSync(handler, S2CPackets.SYNC_DATA_COMPONENT_TYPE_ID, BuiltInRegistries.DATA_COMPONENT_TYPE);
        sendSync(handler, S2CPackets.SYNC_ENCHANTMENT_COMPONENT_TYPE_ID, BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE);

        if (fullSync) {
            sendSync(handler, S2CPackets.SYNC_TAGS_ID, (Registry<Registry<Object>>) BuiltInRegistries.REGISTRY, true, PolymerTagEntry::of);
        }

        PolymerSyncUtils.ON_SYNC_CUSTOM.invoke((c) -> c.accept(handler, fullSync));

        PolymerSyncUtils.ON_SYNC_FINISHED.invoke((c) -> c.accept(handler));

        handler.send(new ClientboundCustomPayloadPacket(new PolymerSyncFinishedS2CPayload()));


        if (PolymerImpl.LOG_SYNC_TIME) {
            PolymerImpl.LOGGER.info((fullSync ? "Full" : "Partial") + " sync for {} took {} ms", handler.player.getGameProfile().name(), ((System.nanoTime() - startTime) / 10000) / 100d);
        }
    }

    private static <T> Collection<T> getServerSideEntries(IdMap<T> registry) {
        if (registry instanceof Registry<T> registry1) {
            return RegistryExtension.getPolymerEntries(registry1);
        } else if (registry instanceof PolymerIdMapper<?>) {
            return ((PolymerIdMapper<T>) registry).polymer$getPolymerEntries();
        }

        return List.of();
    }

    private static void sendSync(ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<PolymerGenericListPayload<IdValueEntry>> packetId, Registry registry) {
        sendSync(handler, packetId, getServerSideEntries(registry), false,
                type -> new IdValueEntry(registry.getId(type), registry.getKey(type)));
    }

    public static void sendCreativeSyncPackets(ServerGamePacketListenerImpl handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version != -1) {
            for (var group : PolymerItemGroupUtils.getItemGroups(handler.getPlayer())) {
                syncItemGroup(group, handler);
            }

            handler.send(new ClientboundCustomPayloadPacket(new PolymerItemGroupApplyUpdateS2CPayload()));
        }
    }

    public static void syncItemGroup(CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        if (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC || PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            removeItemGroup(group, handler);
            syncItemGroupDefinition(group, handler);
        }

        syncItemGroupContents(group, handler);
    }

    public static void syncItemGroupContents(CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);

        if (version != -1) {
            var id = PolymerItemGroupUtils.getId(group);
            if (id == null) {
                return;
            }
            handler.send(new ClientboundCustomPayloadPacket(new PolymerItemGroupContentClearS2CPayload(id)));

            try {
                var entry = PolymerItemGroupContentAddS2CPayload.of(version, group, handler);
                if (entry.isNonEmpty()) {
                    handler.send(new ClientboundCustomPayloadPacket(entry));
                }
            } catch (Exception e) {

            }
        }

    }

    public static void syncItemGroupDefinition(CreativeModeTab group, ServerGamePacketListenerImpl handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version > -1 && (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC || PolymerItemGroupUtils.isPolymerItemGroup(group))) {
            var id = PolymerItemGroupUtils.getId(group);
            if (id != null) {
                handler.send(new ClientboundCustomPayloadPacket(new PolymerItemGroupDefineS2CPayload(id, group.getDisplayName(), group.getIconItem())));
            }
        }
    }

    public static void removeItemGroup(CreativeModeTab group, ServerGamePacketListenerImpl player) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.SYNC_ITEM_GROUP_REMOVE);

        if (version > -1 && PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            var x = PolymerItemGroupUtils.REGISTRY.getEntryId(group);
            if (x != null) {
                player.send(new ClientboundCustomPayloadPacket(new PolymerItemGroupRemoveS2CPayload(x)));
            }
        }
    }

    public static void sendEntityInfo(ServerGamePacketListenerImpl player, Entity entity) {
        sendEntityInfo(player, entity.getId(), entity.getType());
    }

    public static void sendEntityInfo(ServerGamePacketListenerImpl player, int id, EntityType<?> type) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_ENTITY);

        if (version != -1) {
            player.send(new ClientboundCustomPayloadPacket(new PolymerEntityS2CPayload(id, BuiltInRegistries.ENTITY_TYPE.getKey(type))));
        }
    }


    private static <T> void sendSync(ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<PolymerGenericListPayload<T>> id, List<T> entries) {
        handler.send(new ClientboundCustomPayloadPacket(new PolymerGenericListPayload<>(id, List.copyOf(entries))));
        entries.clear();
    }

    private static <T, A> void sendSync(ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<PolymerGenericListPayload<A>> packetId, Iterable<T> iterable, boolean bypassPolymerCheck, Function<T, A> writableFunction) {
        sendSync(handler, packetId, iterable, bypassPolymerCheck, (a, b, c) -> writableFunction.apply(a));
    }

    private static <T, A> void sendSync(ServerGamePacketListenerImpl handler, CustomPacketPayload.Type<PolymerGenericListPayload<A>> packetId, Iterable<T> iterable, boolean bypassPolymerCheck, BufferWritableCreator<T, A> writableFunction) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, packetId.id());

        Registry<T> registry = null;

        if (iterable instanceof RegistryExtension && !bypassPolymerCheck) {
            iterable = ((RegistryExtension<T>) iterable).polymer$getEntries();
            registry = (Registry<T>) iterable;
        }

        if (version != -1) {
            var entries = new ArrayList<A>();
            var ctx = handler.getPacketContext();
            for (var entry : iterable) {
                if (!bypassPolymerCheck || PolymerSyncedObject.canSynchronizeToPolymerClient(registry, entry, ctx)) {
                    var val = writableFunction.serialize(entry, handler, version);
                    if (val != null) {
                        entries.add(val);
                    }

                    if (entries.size() > 100) {
                        sendSync(handler, packetId, entries);
                    }
                }
            }

            if (!entries.isEmpty()) {
                sendSync(handler, packetId, entries);
            }
        }
    }

    public static void sendDebugValidateStatesPackets(ServerGamePacketListenerImpl handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.DEBUG_VALIDATE_STATES);

        if (version != -1) {
            sendSync(handler, S2CPackets.DEBUG_VALIDATE_STATES_ID, Block.BLOCK_STATE_REGISTRY, true, DebugBlockStateEntry::of);
        }
    }

    public interface BufferWritableCreator<T, A> {
        A serialize(T object, ServerGamePacketListenerImpl handler, int version);
    }
}
