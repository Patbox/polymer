package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.payloads.*;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@ApiStatus.Internal
public class PolymerServerProtocol {
    public static void sendBlockUpdate(ServerPlayNetworkHandler player, BlockPos pos, BlockState state) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_SET_BLOCK_UPDATE);

        if (PolymerImplUtils.POLYMER_STATES.contains(state) && version > -1) {
            player.sendPacket(new CustomPayloadS2CPacket(new PolymerBlockUpdateS2CPayload(pos, Block.STATE_IDS.getRawId(state))));
        }
    }

    public static void sendMultiBlockUpdate(ServerPlayNetworkHandler player, ChunkSectionPos chunkPos, short[] positions, BlockState[] blockStates) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var blocks = new IntArrayList();
            var pos = new ShortArrayList();

            for (int i = 0; i < blockStates.length; i++) {
                if (PolymerImplUtils.POLYMER_STATES.contains(blockStates[i])) {
                    blocks.add(Block.STATE_IDS.getRawId(blockStates[i]));
                    pos.add(positions[i]);
                }
            }

            if (!blocks.isEmpty()) {
                player.sendPacket(new CustomPayloadS2CPacket(new PolymerSectionUpdateS2CPayload(chunkPos, pos.toShortArray(), blocks.toIntArray())));
            }
        }
    }

    public static void sendSectionUpdate(ServerPlayNetworkHandler player, WorldChunk chunk) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var wci = (PolymerBlockPosStorage) chunk;
            if (wci.polymer$hasAny()) {
                var sections = chunk.getSectionArray();
                for (var i = 0; i < sections.length; i++) {
                    var section = sections[i];
                    var storage = (PolymerBlockPosStorage) section;

                    if (section != null && storage.polymer$hasAny()) {
                        var set = storage.polymer$getBackendSet();
                        var blocks = new IntArrayList();


                        assert set != null;
                        for (var pos : set) {
                            int x = ChunkSectionPos.unpackLocalX(pos);
                            int y = ChunkSectionPos.unpackLocalY(pos);
                            int z = ChunkSectionPos.unpackLocalZ(pos);
                            var state = section.getBlockState(x, y, z);
                            blocks.add(Block.STATE_IDS.getRawId(state));
                        }

                        player.sendPacket(new CustomPayloadS2CPacket(new PolymerSectionUpdateS2CPayload(ChunkSectionPos.from(chunk.getPos(), chunk.sectionIndexToCoord(i)),
                                set.toShortArray(), blocks.toIntArray())));
                    }
                }
            }
        }
    }


    public static void sendSyncPackets(ServerPlayNetworkHandler handler, boolean fullSync) {
        if (PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_STARTED) == -1) {
            return;
        }

        var startTime = System.nanoTime();
        int version;

        handler.sendPacket(new CustomPayloadS2CPacket(new PolymerSyncStartedS2CPayload()));
        PolymerSyncUtils.ON_SYNC_STARTED.invoke((c) -> c.accept(handler));

        version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_CLEAR);
        if (version != -1) {
            handler.sendPacket(new CustomPayloadS2CPacket(new PolymerSyncClearS2CPayload()));
        }

        PolymerSyncUtils.BEFORE_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_ITEM_ID, getServerSideEntries(Registries.ITEM), false, PolymerItemEntry::of);
        PolymerSyncUtils.AFTER_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        if (fullSync) {
            PolymerSyncUtils.BEFORE_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));

            sendCreativeSyncPackets(handler);

            PolymerSyncUtils.AFTER_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));
        }

        PolymerSyncUtils.BEFORE_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_BLOCK_ID, getServerSideEntries(Registries.BLOCK), false, PolymerBlockEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        PolymerSyncUtils.BEFORE_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_BLOCKSTATE_ID, getServerSideEntries(Block.STATE_IDS), false, PolymerBlockStateEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        PolymerSyncUtils.BEFORE_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, S2CPackets.SYNC_ENTITY_ID, getServerSideEntries(Registries.ENTITY_TYPE), false, PolymerEntityEntry::of);
        PolymerSyncUtils.AFTER_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        sendSync(handler, S2CPackets.SYNC_VILLAGER_PROFESSION_ID, Registries.VILLAGER_PROFESSION);
        sendSync(handler, S2CPackets.SYNC_STATUS_EFFECT_ID, Registries.STATUS_EFFECT);
        sendSync(handler, S2CPackets.SYNC_BLOCK_ENTITY_ID, Registries.BLOCK_ENTITY_TYPE);
        sendSync(handler, S2CPackets.SYNC_FLUID_ID, Registries.FLUID);

        if (fullSync) {
            sendSync(handler, S2CPackets.SYNC_TAGS_ID, (Registry<Registry<Object>>) Registries.REGISTRIES, true, PolymerTagEntry::of);
        }

        PolymerSyncUtils.ON_SYNC_CUSTOM.invoke((c) -> c.accept(handler, fullSync));

        PolymerSyncUtils.ON_SYNC_FINISHED.invoke((c) -> c.accept(handler));

        handler.sendPacket(new CustomPayloadS2CPacket(new PolymerSyncFinishedS2CPayload()));


        if (PolymerImpl.LOG_SYNC_TIME) {
            PolymerImpl.LOGGER.info((fullSync ? "Full" : "Partial") + " sync for {} took {} ms", handler.player.getGameProfile().getName(), ((System.nanoTime() - startTime) / 10000) / 100d);
        }
    }

    private static <T> Collection<T> getServerSideEntries(IndexedIterable<T> registry) {
        if (registry instanceof Registry<T> registry1) {
            return RegistryExtension.getPolymerEntries(registry1);
        } else if (registry instanceof PolymerIdList<?>) {
            return ((PolymerIdList<T>) registry).polymer$getPolymerEntries();
        }

        return List.of();
    }

    private static void sendSync(ServerPlayNetworkHandler handler, CustomPayload.Id<PolymerGenericListPayload<IdValueEntry>> packetId, Registry registry) {
        sendSync(handler, packetId, getServerSideEntries(registry), false,
                type -> new IdValueEntry(registry.getRawId(type), registry.getId(type)));
    }

    public static void sendCreativeSyncPackets(ServerPlayNetworkHandler handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version != -1) {
            for (var group : PolymerItemGroupUtils.getItemGroups(handler.getPlayer())) {
                syncItemGroup(group, handler);
            }

            handler.sendPacket(new CustomPayloadS2CPacket(new PolymerItemGroupApplyUpdateS2CPayload()));
        }
    }

    public static void syncItemGroup(ItemGroup group, ServerPlayNetworkHandler handler) {
        if (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC || PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            removeItemGroup(group, handler);
            syncItemGroupDefinition(group, handler);
        }

        syncItemGroupContents(group, handler);
    }

    public static void syncItemGroupContents(ItemGroup group, ServerPlayNetworkHandler handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);

        if (version != -1) {
            var id = PolymerItemGroupUtils.getId(group);
            handler.sendPacket(new CustomPayloadS2CPacket(new PolymerItemGroupContentClearS2CPayload(id)));



            try {
                var entry = PolymerItemGroupContentAddS2CPayload.of(group, handler);
                if (entry.isNonEmpty()) {
                    handler.sendPacket(new CustomPayloadS2CPacket(entry));
                }
            } catch (Exception e) {

            }
        }

    }

    public static void syncItemGroupDefinition(ItemGroup group, ServerPlayNetworkHandler handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version > -1 && (PolymerImpl.SYNC_MODDED_ENTRIES_POLYMC || PolymerItemGroupUtils.isPolymerItemGroup(group))) {
            handler.sendPacket(new CustomPayloadS2CPacket(new PolymerItemGroupDefineS2CPayload(PolymerItemGroupUtils.getId(group), group.getDisplayName(), group.getIcon())));
        }
    }

    public static void removeItemGroup(ItemGroup group, ServerPlayNetworkHandler player) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.SYNC_ITEM_GROUP_REMOVE);

        if (version > -1 && PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            player.sendPacket(new CustomPayloadS2CPacket(new PolymerItemGroupRemoveS2CPayload(PolymerItemGroupUtils.REGISTRY.getId(group))));
        }
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, Entity entity) {
        sendEntityInfo(player, entity.getId(), entity.getType());
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, int id, EntityType<?> type) {
        var version = PolymerServerNetworking.getSupportedVersion(player, S2CPackets.WORLD_ENTITY);

        if (version != -1) {
            player.sendPacket(new CustomPayloadS2CPacket(new PolymerEntityS2CPayload(id, Registries.ENTITY_TYPE.getId(type))));
        }
    }


    private static <T> void sendSync(ServerPlayNetworkHandler handler, CustomPayload.Id<PolymerGenericListPayload<T>> id, List<T> entries) {
        handler.sendPacket(new CustomPayloadS2CPacket(new PolymerGenericListPayload<>(id, List.copyOf(entries))));
        entries.clear();
    }

    private static <T, A> void sendSync(ServerPlayNetworkHandler handler, CustomPayload.Id<PolymerGenericListPayload<A>> packetId, Iterable<T> iterable, boolean bypassPolymerCheck, Function<T, A> writableFunction) {
        sendSync(handler, packetId, iterable, bypassPolymerCheck, (a, b, c) -> writableFunction.apply(a));
    }

    private static <T, A> void sendSync(ServerPlayNetworkHandler handler, CustomPayload.Id<PolymerGenericListPayload<A>> packetId, Iterable<T> iterable, boolean bypassPolymerCheck, BufferWritableCreator<T, A> writableFunction) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, packetId.id());

        if (iterable instanceof RegistryExtension && !bypassPolymerCheck) {
            iterable = ((RegistryExtension<T>) iterable).polymer$getEntries();
        }

        if (version != -1) {
            var entries = new ArrayList<A>();
            for (var entry : iterable) {
                if (!bypassPolymerCheck || (entry instanceof PolymerSyncedObject<?> obj && obj.canSynchronizeToPolymerClient(handler.player))) {
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

    public static void sendDebugValidateStatesPackets(ServerPlayNetworkHandler handler) {
        var version = PolymerServerNetworking.getSupportedVersion(handler, S2CPackets.DEBUG_VALIDATE_STATES);

        if (version != -1) {
            sendSync(handler, S2CPackets.DEBUG_VALIDATE_STATES_ID, Block.STATE_IDS, true, DebugBlockStateEntry::of);
        }
    }

    public interface BufferWritableCreator<T, A> {
        A serialize(T object, ServerPlayNetworkHandler handler, int version);
    }
}
