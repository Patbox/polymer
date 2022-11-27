package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.core.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import eu.pb4.polymer.core.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.core.impl.networking.packets.*;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static eu.pb4.polymer.core.api.networking.PolymerPacketUtils.buf;

@ApiStatus.Internal
public class PolymerServerProtocol {
    public static void sendHandshake(PolymerHandshakeHandler handler) {
        var buf = buf(0);

        buf.writeString(PolymerImpl.VERSION);
        buf.writeVarInt(ClientPackets.REGISTRY.size());

        for (var id : ClientPackets.REGISTRY.keySet()) {
            buf.writeString(id);

            var entry = ClientPackets.REGISTRY.get(id);

            buf.writeVarInt(entry.length);
            for (int i : entry) {
                buf.writeVarInt(i);
            }
        }

        handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.HANDSHAKE_ID, buf));
    }

    public static void sendBlockUpdate(ServerPlayNetworkHandler player, BlockPos pos, BlockState state) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.WORLD_SET_BLOCK_UPDATE);

        if (state.getBlock() instanceof PolymerBlock && version > -1) {
            var buf = buf(version);

            buf.writeBlockPos(pos);
            buf.writeVarInt(Block.STATE_IDS.getRawId(state));

            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_SET_BLOCK_UPDATE_ID, buf));
        }

    }

    public static void sendMultiBlockUpdate(ServerPlayNetworkHandler player, ChunkSectionPos chunkPos, short[] positions, BlockState[] blockStates) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var list = new LongArrayList();

            for (int i = 0; i < blockStates.length; i++) {
                if (blockStates[i].getBlock() instanceof PolymerBlock) {
                    list.add(((long) Block.STATE_IDS.getRawId(blockStates[i])) << 12 | positions[i]);
                }
            }

            if (list.size() != 0) {
                var buf = buf(version);
                buf.writeChunkSectionPos(chunkPos);
                buf.writeVarInt(list.size());

                for (var value : list) {
                    buf.writeVarLong(value);
                }

                player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_CHUNK_SECTION_UPDATE_ID, buf));
            }
        }
    }

    public static void sendSectionUpdate(ServerPlayNetworkHandler player, WorldChunk chunk) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version > -1) {
            var wci = (PolymerBlockPosStorage) chunk;
            if (wci.polymer$hasAny()) {
                for (var section : chunk.getSectionArray()) {
                    var storage = (PolymerBlockPosStorage) section;

                    if (section != null && storage.polymer$hasAny()) {
                        var buf = buf(version);
                        var set = storage.polymer$getBackendSet();
                        buf.writeChunkSectionPos(ChunkSectionPos.from(chunk.getPos(), ChunkSectionPos.getSectionCoord(section.getYOffset())));

                        assert set != null;
                        var size = set.size();
                        buf.writeVarInt(size);

                        for (var pos : set) {
                            int x = ChunkSectionPos.unpackLocalX(pos);
                            int y = ChunkSectionPos.unpackLocalY(pos);
                            int z = ChunkSectionPos.unpackLocalZ(pos);
                            var state = section.getBlockState(x, y, z);
                            buf.writeVarLong((((long) Block.STATE_IDS.getRawId(state)) << 12 | pos));
                        }

                        player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_CHUNK_SECTION_UPDATE_ID, buf));
                    }
                }
            }
        }
    }


    public static void sendSyncPackets(ServerPlayNetworkHandler handler, boolean fullSync) {
        var startTime = System.nanoTime();

        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);

        if (!polymerHandler.polymer$hasPolymer()) {
            return;
        }
        int version;

        handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_STARTED_ID, buf(0)));
        PolymerSyncUtils.ON_SYNC_STARTED.invoke((c) -> c.accept(handler));

        version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_CLEAR);
        if (version == 0) {
            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_CLEAR_ID, buf(version)));
        }

        version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_INFO);
        if (version == 0) {
            var buf = buf(version);

            buf.writeVarInt(PolymerImplUtils.getBlockStateOffset());

            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_INFO_ID, buf));
        }
        sendSync(handler, ServerPackets.SYNC_ENCHANTMENT_ID, RegistryExtension.getPolymerEntries(Registries.ENCHANTMENT), false,
                type -> new IdValueEntry(Registries.ENCHANTMENT.getRawId(type), Registries.ENCHANTMENT.getId(type)));

        PolymerSyncUtils.BEFORE_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, ServerPackets.SYNC_ITEM_ID, RegistryExtension.getPolymerEntries(Registries.ITEM), false, PolymerItemEntry::of);
        PolymerSyncUtils.AFTER_ITEM_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        if (fullSync) {
            PolymerSyncUtils.BEFORE_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));

            sendCreativeSyncPackets(handler);

            PolymerSyncUtils.AFTER_ITEM_GROUP_SYNC.invoke((listener) -> listener.accept(handler, true));
        }

        PolymerSyncUtils.BEFORE_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, ServerPackets.SYNC_BLOCK_ID, RegistryExtension.getPolymerEntries(Registries.BLOCK), false, PolymerBlockEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_SYNC.invoke((listener) -> listener.accept(handler, fullSync));

        PolymerSyncUtils.BEFORE_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, ServerPackets.SYNC_BLOCKSTATE_ID, ((PolymerIdList) Block.STATE_IDS).polymer$getPolymerStates(), false, PolymerBlockStateEntry::of);
        PolymerSyncUtils.AFTER_BLOCK_STATE_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        PolymerSyncUtils.BEFORE_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));
        sendSync(handler, ServerPackets.SYNC_ENTITY_ID, RegistryExtension.getPolymerEntries(Registries.ENTITY_TYPE), false, PolymerEntityEntry::of);
        PolymerSyncUtils.AFTER_ENTITY_SYNC.invoke((listener) -> listener.accept(handler, fullSync));


        sendSync(handler, ServerPackets.SYNC_VILLAGER_PROFESSION_ID, RegistryExtension.getPolymerEntries(Registries.VILLAGER_PROFESSION), false,
                entry -> new IdValueEntry(Registries.VILLAGER_PROFESSION.getRawId(entry), Registries.VILLAGER_PROFESSION.getId(entry)));

        sendSync(handler, ServerPackets.SYNC_STATUS_EFFECT_ID, RegistryExtension.getPolymerEntries(Registries.STATUS_EFFECT), false,
                entry -> new IdValueEntry(Registries.STATUS_EFFECT.getRawId(entry), Registries.STATUS_EFFECT.getId(entry)));

        sendSync(handler, ServerPackets.SYNC_BLOCK_ENTITY_ID, RegistryExtension.getPolymerEntries(Registries.BLOCK_ENTITY_TYPE), false,
                type -> new IdValueEntry(Registries.BLOCK_ENTITY_TYPE.getRawId(type), Registries.BLOCK_ENTITY_TYPE.getId(type)));

        if (fullSync) {
            sendSync(handler, ServerPackets.SYNC_TAGS_ID, (Registry<Registry<Object>>) Registries.REGISTRIES, true, PolymerTagEntry::of);
        }

        PolymerSyncUtils.ON_SYNC_CUSTOM.invoke((c) -> c.accept(handler, fullSync));

        PolymerSyncUtils.ON_SYNC_FINISHED.invoke((c) -> c.accept(handler));

        handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_FINISHED_ID, buf(0)));


        if (PolymerImpl.LOG_SYNC_TIME) {
            PolymerImpl.LOGGER.info((fullSync ? "Full" : "Partial") + " sync for {} took {} ms", handler.player.getGameProfile().getName(), ((System.nanoTime() - startTime) / 10000) / 100d);
        }
    }

    public static void sendCreativeSyncPackets(ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version != -1) {
            for (var group : PolymerItemGroupUtils.getItemGroups(handler.getPlayer())) {
                syncItemGroup(group, handler);
            }

            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_APPLY_UPDATE_ID, buf(polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_APPLY_UPDATE))));
        }
    }

    public static void syncItemGroup(ItemGroup group, ServerPlayNetworkHandler handler) {
        if (PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            removeItemGroup(group, handler);
            syncItemGroupDefinition(group, handler);
        }

        syncItemGroupContents(group, handler);
    }

    public static void syncItemGroupContents(ItemGroup group, ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);

        if (version != -1) {
            PolymerImplUtils.setPlayer(handler.player);
            version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_ADD);

            {
                var buf = buf(polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR));
                buf.writeIdentifier(PolymerImplUtils.toItemGroupId(group));
                handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR_ID, buf));
            }


            try {
                var entry = PolymerItemGroupContent.of(group, handler);
                if (entry.isNonEmpty()) {
                    var buf = buf(version);
                    entry.write(buf, version, handler);
                    handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_CONTENTS_ADD_ID, buf));
                }
            } catch (Exception e) {

            }
        }

        PolymerImplUtils.setPlayer(null);
    }

    public static void syncItemGroupDefinition(ItemGroup group, ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_DEFINE);

        if (version > -1 && PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            var buf = buf(version);
            PolymerImplUtils.setPlayer(handler.player);

            buf.writeIdentifier(PolymerImplUtils.toItemGroupId(group));
            buf.writeText(ServerTranslationUtils.parseFor(handler, group.getDisplayName()));
            PolymerImplUtils.writeStack(buf, ServerTranslationUtils.parseFor(handler, PolymerItemUtils.getPolymerItemStack(group.getIcon(), handler.player)));

            handler.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_DEFINE_ID, buf));
            PolymerImplUtils.setPlayer(null);
        }
    }

    public static void removeItemGroup(ItemGroup group, ServerPlayNetworkHandler player) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_REMOVE);

        if (version > -1 && PolymerItemGroupUtils.isPolymerItemGroup(group)) {
            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_REMOVE_ID, buf(version).writeIdentifier(PolymerImplUtils.toItemGroupId(group))));
        }
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, Entity entity) {
        sendEntityInfo(player, entity.getId(), entity.getType());
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, int id, EntityType<?> type) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.WORLD_ENTITY);

        if (version == 0) {
            var buf = buf(0);
            buf.writeVarInt(id);
            buf.writeIdentifier(Registries.ENTITY_TYPE.getId(type));
            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_ENTITY_ID, buf));
        }
    }


    private static void sendSync(ServerPlayNetworkHandler handler, Identifier id, int version, List<BufferWritable> entries) {
        var buf = buf(version);

        buf.writeVarInt(entries.size());
        PolymerImplUtils.setPlayer(handler.player);
        for (var entry : entries) {
            entry.write(buf, version, handler);
        }
        PolymerImplUtils.setPlayer(null);

        handler.sendPacket(new CustomPayloadS2CPacket(id, buf));
        entries.clear();
    }

    private static <T> void sendSync(ServerPlayNetworkHandler handler, Identifier packetId, Iterable<T> iterable, boolean bypassPolymerCheck, Function<T, BufferWritable> writableFunction) {
        sendSync(handler, packetId, iterable, bypassPolymerCheck, (a, b, c) -> writableFunction.apply(a));
    }

    private static <T> void sendSync(ServerPlayNetworkHandler handler, Identifier packetId, Iterable<T> iterable, boolean bypassPolymerCheck, BufferWritableCreator<T> writableFunction) {
        var version = PolymerNetworkHandlerExtension.of(handler).polymer$getSupportedVersion(packetId.getPath());

        if (iterable instanceof RegistryExtension && !bypassPolymerCheck) {
            iterable = ((RegistryExtension<T>) iterable).polymer$getEntries();
        }

        if (version != -1) {
            var entries = new ArrayList<BufferWritable>();
            for (var entry : iterable) {
                if (!bypassPolymerCheck || (entry instanceof PolymerSyncedObject<?> obj && obj.canSynchronizeToPolymerClient(handler.player))) {
                    var val = writableFunction.serialize(entry, handler, version);
                    if (val != null) {
                        entries.add(val);
                    }

                    if (entries.size() > 100) {
                        sendSync(handler, packetId, version, entries);
                    }
                }
            }

            if (entries.size() != 0) {
                sendSync(handler, packetId, version, entries);
            }
        }
    }

    public static void sendDebugValidateStatesPackets(ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var version = polymerHandler.polymer$getSupportedVersion(ServerPackets.DEBUG_VALIDATE_STATES);

        if (version != -1) {
            sendSync(handler, ServerPackets.DEBUG_VALIDATE_STATES_ID, Block.STATE_IDS, true, DebugBlockStateEntry::of);
        }
    }

    public interface BufferWritableCreator<T> {
        BufferWritable serialize(T object, ServerPlayNetworkHandler handler, int version);
    }
}
