package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.api.networking.PolymerSyncUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.impl.interfaces.NetworkIdList;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.networking.packets.*;
import eu.pb4.polymer.impl.entity.InternalEntityHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static eu.pb4.polymer.api.networking.PolymerPacketUtils.buf;

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
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.WORLD_SET_BLOCK_UPDATE);

        if (version == 0) {
            var buf = buf(0);

            buf.writeBlockPos(pos);
            buf.writeVarInt(getRawId(state, player.player));

            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_SET_BLOCK_UPDATE_ID, buf));
        }

    }

    public static void sendMultiBlockUpdate(ServerPlayNetworkHandler player, ChunkSectionPos chunkPos, short[] positions, BlockState[] blockStates) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.WORLD_CHUNK_SECTION_UPDATE);

        if (version == 0) {
            var buf = buf(0);

            buf.writeChunkSectionPos(chunkPos);
            buf.writeVarInt(positions.length);

            for (int i = 0; i < blockStates.length; i++) {
                buf.writeVarLong(((long) getRawId(blockStates[i], player.player) << 12 | positions[i]));
            }

            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_CHUNK_SECTION_UPDATE_ID, buf));
        }
    }

    public static void sendSectionUpdate(ServerPlayNetworkHandler player, WorldChunk chunk) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.WORLD_SET_BLOCK_UPDATE);

        if (version == 0) {
            var wci = (PolymerBlockPosStorage) chunk;
            if (wci.polymer_hasAny()) {
                for (var section : chunk.getSectionArray()) {
                    var storage = (PolymerBlockPosStorage) section;

                    if (section != null && storage.polymer_hasAny()) {
                        var buf = buf(0);
                        var set = storage.polymer_getBackendSet();
                        buf.writeChunkSectionPos(ChunkSectionPos.from(chunk.getPos(), section.getYOffset() >> 4));

                        assert set != null;
                        var size = set.size();
                        buf.writeVarInt(size);

                        for (var pos : set) {
                            int x = ChunkSectionPos.unpackLocalX(pos);
                            int y = ChunkSectionPos.unpackLocalY(pos);
                            int z = ChunkSectionPos.unpackLocalZ(pos);

                            buf.writeVarLong(((long) getRawId(section.getBlockState(x, y, z), player.player) << 12 | pos));
                        }

                        player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_CHUNK_SECTION_UPDATE_ID, buf));
                    }
                }
            }
        }
    }


    public static void sendSyncPackets(ServerPlayNetworkHandler player, boolean fullSync) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (!polymerHandler.polymer_hasPolymer()) {
            return;
        }
        int version;

        player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_STARTED_ID, buf(0)));
        PolymerSyncUtils.ON_SYNC_STARTED.invoke((c) -> c.accept(player));

        version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_CLEAR);
        if (version == 0) {
            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_CLEAR_ID, buf(version)));
        }


        version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ITEM);

        var entries = new ArrayList<BufferWritable>();
        if (version != -1) {
            for (var entry : Registry.ITEM) {
                if (entry instanceof PolymerItem obj && obj.shouldSyncWithPolymerClient(player.player)) {
                    entries.add(PolymerItemEntry.of(entry, player));

                    if (entries.size() > 40) {
                        sendSync(player, ServerPackets.SYNC_ITEM_ID, version, entries);
                    }
                }
            }

            if (entries.size() != 0) {
                sendSync(player, ServerPackets.SYNC_ITEM_ID, version, entries);
            }
        }

        if (fullSync) {
            for (var group : InternalServerRegistry.ITEM_GROUPS) {
                if (group.shouldSyncWithPolymerClient(player.player)) {
                    syncItemGroup(group, player);
                }
            }
        }

        version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_BLOCK);
        if (version != -1) {
            for (var entry : Registry.BLOCK) {
                if (entry instanceof PolymerBlock obj && obj.shouldSyncWithPolymerClient(player.player)) {
                    entries.add(PolymerBlockEntry.of(entry));

                    if (entries.size() > 40) {
                        sendSync(player, ServerPackets.SYNC_BLOCK_ID, version, entries);
                    }
                }
            }

            if (entries.size() != 0) {
                sendSync(player, ServerPackets.SYNC_BLOCK_ID, version, entries);
            }
        }
        version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_BLOCKSTATE);
        if (version != -1) {
            var list = ((NetworkIdList) Block.STATE_IDS).polymer_getInternalList().getOffsetList();
            for (BlockState entry : list) {
                if (entry != null && ((PolymerObject) entry.getBlock()).shouldSyncWithPolymerClient(player.player)) {
                    entries.add(PolymerBlockStateEntry.of(entry, player));

                    if (entries.size() > 40) {
                        sendSync(player, ServerPackets.SYNC_BLOCKSTATE_ID, version, entries);
                    }
                }
            }

            if (entries.size() != 0) {
                sendSync(player, ServerPackets.SYNC_BLOCKSTATE_ID, version, entries);
            }
        }

        version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ENTITY);
        if (version != -1) {
            for (var entry : Registry.ENTITY_TYPE) {
                var internalEntity = InternalEntityHelpers.getEntity(entry);
                if (entry != null && internalEntity instanceof PolymerEntity obj && obj.shouldSyncWithPolymerClient(player.player)) {
                    entries.add(PolymerEntityEntry.of(entry));

                    if (entries.size() > 40) {
                        sendSync(player, ServerPackets.SYNC_ENTITY_ID, version, entries);
                    }
                }
            }

            if (entries.size() != 0) {
                sendSync(player, ServerPackets.SYNC_ENTITY_ID, version, entries);
            }
        }

        PolymerSyncUtils.ON_SYNC_CUSTOM.invoke((c) -> c.accept(player, fullSync));

        PolymerSyncUtils.ON_SYNC_FINISHED.invoke((c) -> c.accept(player));

        player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_FINISHED_ID, buf(0)));
    }


    public static void sendCreativeSyncPackets(ServerPlayNetworkHandler handler) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(handler);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP);

        if (version != -1) {
            for (var group : PolymerUtils.getItemGroups(handler.getPlayer())) {
                syncItemGroup(group, handler);
            }
        }
    }

    public static void syncVanillaItemGroups(ServerPlayNetworkHandler player) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_VANILLA);

        if (version != -1) {
            for (var group : ItemGroup.GROUPS) {
                try {
                    if (!(group instanceof PolymerObject)) {
                        var buf = buf(version);
                        PolymerVanillaItemGroupEntry.of(group, player).write(buf, version, player);
                        player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_VANILLA_ID, buf));
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
    }

    public static void syncItemGroup(PolymerItemGroup group, ServerPlayNetworkHandler player) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP);

        if (version == 0) {
            var buf = buf(version);

            var list = DefaultedList.<ItemStack>of();
            group.appendStacks(list);

            buf.writeIdentifier(group.getId());
            buf.writeText(ServerTranslationUtils.parseFor(player, group.getDisplayName()));
            buf.writeItemStack(ServerTranslationUtils.parseFor(player, PolymerItemUtils.getPolymerItemStack(group.createIcon(), player.player)));
            buf.writeVarInt(list.size());
            for (var stack : list) {
                buf.writeItemStack(ServerTranslationUtils.parseFor(player, PolymerItemUtils.getPolymerItemStack(stack, player.player)));
            }

            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_ID, buf));
        }
    }

    public static void removeItemGroup(PolymerItemGroup group, ServerPlayNetworkHandler player) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.SYNC_ITEM_GROUP_REMOVE);

        if (version == 0) {
            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.SYNC_ITEM_GROUP_REMOVE_ID, buf(0).writeIdentifier(group.getId())));
        }
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, Entity entity) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);
        var version = polymerHandler.polymer_getSupportedVersion(ServerPackets.WORLD_ENTITY);

        if (version == 0) {
            var buf = buf(0);
            buf.writeVarInt(entity.getId());
            buf.writeIdentifier(Registry.ENTITY_TYPE.getId(entity.getType()));
            player.sendPacket(new CustomPayloadS2CPacket(ServerPackets.WORLD_ENTITY_ID, buf));
        }
    }


    private static void sendSync(ServerPlayNetworkHandler handler, Identifier id, int version, List<BufferWritable> entries) {
        var buf = buf(version);

        buf.writeVarInt(entries.size());

        for (var entry : entries) {
            entry.write(buf, version, handler);
        }

        handler.sendPacket(new CustomPayloadS2CPacket(id, buf));
        entries.clear();
    }

    public static int getRawId(BlockState state, ServerPlayerEntity player) {
        return state.getBlock() instanceof PolymerBlock polymerBlock && polymerBlock.shouldSyncWithPolymerClient(player) ? Block.STATE_IDS.getRawId(state) - PolymerBlockUtils.BLOCK_STATE_OFFSET + 1 : 0;
    }

    @Nullable
    public static BlockState getBlockState(int id) {
        return Block.STATE_IDS.get(id + PolymerBlockUtils.BLOCK_STATE_OFFSET - 1);
    }
}
