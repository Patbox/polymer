package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.impl.InternalServerRegistry;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.compat.ServerTranslationUtils;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.impl.networking.packets.*;
import eu.pb4.polymer.impl.interfaces.NetworkIdList;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.other.InternalEntityHelpers;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;


import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
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
import java.util.HashSet;
import java.util.List;

@ApiStatus.Internal
public class ServerPacketBuilders {
    public static PacketByteBuf buf() {
        return new PacketByteBuf(Unpooled.buffer());
    }

    public static void createSingleBlockPacket(ServerPlayNetworkHandler player, BlockPos pos, BlockState state) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (polymerHandler.polymer_hasPolymer()) {
            var buf = buf();

            buf.writeBlockPos(pos);
            buf.writeVarInt(getRawId(state, player.player));

            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.BLOCK_UPDATE_ID, buf));
        }
    }

    public static void createMultiBlockPacket(ServerPlayNetworkHandler player, ChunkSectionPos chunkPos, short[] positions, BlockState[] blockStates) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (polymerHandler.polymer_hasPolymer()) {
            var buf = buf();

            buf.writeChunkSectionPos(chunkPos);
            buf.writeVarInt(positions.length);

            for (int i = 0; i < blockStates.length; i++) {
                buf.writeVarLong((getRawId(blockStates[i], player.player) << 12 | positions[i]));
            }

            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.CHUNK_SECTION_UPDATE_ID, buf));
        }
    }

    public static void createChunkPacket(ServerPlayNetworkHandler player, @Nullable ChunkDataS2CPacket packet, WorldChunk chunk) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (polymerHandler.polymer_hasPolymer()) {
            var pi = (ChunkDataS2CPacketInterface) packet;
            var packets = packet != null ? pi.polymer_getPolymerSyncPackets() : null;

            if (packets == null) {
                var wci = (PolymerBlockPosStorage) chunk;

                if (wci.polymer_hasAny()) {
                    var list = new ArrayList<Packet<?>>();

                    for (var section : chunk.getSectionArray()) {
                        var storage = (PolymerBlockPosStorage) section;

                        if (section != null && storage.polymer_hasAny()) {
                            var buf = buf();
                            buf.writeChunkSectionPos(ChunkSectionPos.from(chunk.getPos(), section.getYOffset() >> 4));

                            var size = storage.polymer_getBackendSet().size();
                            buf.writeVarInt(size);

                            for (var pos : storage.polymer_getBackendSet()) {
                                int x = ChunkSectionPos.unpackLocalX(pos);
                                int y = ChunkSectionPos.unpackLocalY(pos);
                                int z = ChunkSectionPos.unpackLocalZ(pos);

                                buf.writeVarLong((getRawId(section.getBlockState(x, y, z), player.player) << 12 | pos));
                            }

                            list.add(new CustomPayloadS2CPacket(PolymerPacketIds.CHUNK_SECTION_UPDATE_ID, buf));
                        }
                    }

                    packets = list.toArray(new Packet[0]);
                } else {
                    packets = new Packet[0];
                }

                if (packet != null) {
                    pi.polymer_setPolymerSyncPackets(packets);
                }
            }

            for (int i = 0; i < packets.length; i++) {
                player.sendPacket(packets[i]);
            }
        }
    }

    public static void createSyncPackets(ServerPlayNetworkHandler player) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (polymerHandler.polymer_hasPolymer()) {
            {
                var buf = buf();
                buf.writeShort(PolymerMod.PROTOCOL_VERSION);
                buf.writeString(PolymerMod.VERSION);
                player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.VERSION_ID, buf));
            }

            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.SYNC_STARTED_ID, buf()));

            var entries = new ArrayList<BufferWritable>();
            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_ITEM_GROUP_CLEAR_ID, buf()));

            {
                for (var entry : Registry.ITEM) {
                    if (entry != null && entry instanceof PolymerItem obj && obj.shouldSyncWithPolymerClient(player.player)) {
                        entries.add(PolymerItemEntry.of(entry, player));

                        if (entries.size() > 40) {
                            sendSync(player, PolymerPacketIds.REGISTRY_ITEM_ID, entries);
                        }
                    }
                }

                if (entries.size() != 0) {
                    sendSync(player, PolymerPacketIds.REGISTRY_ITEM_ID, entries);
                }
            }
            {
                for (var group : ItemGroup.GROUPS) {
                    try {
                        if (!(group instanceof PolymerObject)) {
                            var buf = buf();
                            PolymerVanillaItemGroupEntry.of(group, player).write(buf, player);
                            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_ITEM_GROUP_VANILLA_ID, buf));
                        }
                    } catch (Exception e) {
                        //
                    }
                }
            }
            {
                for (var group : InternalServerRegistry.ITEM_GROUPS) {
                    if (group.shouldSyncWithPolymerClient(player.player)) {
                        syncItemGroup(group, player);
                    }
                }
            }
            {
                for (var entry : Registry.BLOCK) {
                    if (entry != null && entry instanceof PolymerBlock obj && obj.shouldSyncWithPolymerClient(player.player)) {
                        entries.add(PolymerBlockEntry.of(entry));

                        if (entries.size() > 40) {
                            sendSync(player, PolymerPacketIds.REGISTRY_BLOCK_ID, entries);
                        }
                    }
                }

                if (entries.size() != 0) {
                    sendSync(player, PolymerPacketIds.REGISTRY_BLOCK_ID, entries);
                }
            }
            {
                var list = ((NetworkIdList) Block.STATE_IDS).polymer_getInternalList().getOffsetList();
                var size = list.size();
                for (int i = 0; i < size; i++) {
                    var entry = list.get(i);

                    if (entry != null && ((PolymerObject) entry.getBlock()).shouldSyncWithPolymerClient(player.player)) {
                        entries.add(PolymerBlockStateEntry.of(entry, player));

                        if (entries.size() > 40) {
                            sendSync(player, PolymerPacketIds.REGISTRY_BLOCKSTATE_ID, entries);
                        }
                    }
                }

                if (entries.size() != 0) {
                    sendSync(player, PolymerPacketIds.REGISTRY_BLOCKSTATE_ID, entries);
                }
            }

            {
                for (var entry : Registry.ENTITY_TYPE) {
                    var internalEntity = InternalEntityHelpers.getEntity(entry);
                    if (entry != null && internalEntity instanceof PolymerEntity obj && obj.shouldSyncWithPolymerClient(player.player)) {
                        entries.add(PolymerEntityEntry.of(entry));

                        if (entries.size() > 40) {
                            sendSync(player, PolymerPacketIds.REGISTRY_ENTITY_ID, entries);
                        }
                    }
                }

                if (entries.size() != 0) {
                    sendSync(player, PolymerPacketIds.REGISTRY_ENTITY_ID, entries);
                }
            }

            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_RESET_SEARCH_ID, buf()));
        }

        PolymerSyncUtils.rebuildCreativeSearch(player);

        player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.SYNC_FINISHED_ID, buf()));
    }

    public static void createCreativeTabSync(ServerPlayNetworkHandler handler) {
        var list = new HashSet<PolymerItemGroup>();


        for (var group : InternalServerRegistry.ITEM_GROUPS) {
            if (group.shouldSyncWithPolymerClient(handler.player)) {
                list.add(group);
            }
        }

        var sync = new PolymerItemGroup.ItemGroupSyncer() {
            @Override
            public void send(PolymerItemGroup group) {
                list.add(group);
            }

            @Override
            public void remove(PolymerItemGroup group) {
                list.remove(group);
            }
        };

        PolymerItemGroup.SYNC_EVENT.invoke((x) -> x.onItemGroupSync(handler.player, sync));

        for (var group : list) {
            syncItemGroup(group, handler);
        }
    }

    public static void syncItemGroup(PolymerItemGroup group, ServerPlayNetworkHandler player) {
        var buf = buf();

        var list = DefaultedList.<ItemStack>of();
        group.appendStacks(list);

        buf.writeIdentifier(group.getId());
        buf.writeText(ServerTranslationUtils.parseFor(player, group.getTranslationKey()));
        buf.writeItemStack(ServerTranslationUtils.parseFor(player, PolymerItemUtils.getPolymerItemStack(group.createIcon(), player.player)));
        buf.writeVarInt(list.size());
        for (var stack : list) {
            buf.writeItemStack(ServerTranslationUtils.parseFor(player, PolymerItemUtils.getPolymerItemStack(stack, player.player)));
        }

        player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_ITEM_GROUP_ID, buf));
    }

    public static void removeItemGroup(PolymerItemGroup group, ServerPlayNetworkHandler player) {
        player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.REGISTRY_ITEM_GROUP_REMOVE_ID, buf().writeIdentifier(group.getId())));
    }

    public static void sendEntityInfo(ServerPlayNetworkHandler player, Entity entity) {
        var polymerHandler = PolymerNetworkHandlerExtension.of(player);

        if (polymerHandler.polymer_hasPolymer()) {
            var buf = buf();
            buf.writeVarInt(entity.getId());
            buf.writeIdentifier(Registry.ENTITY_TYPE.getId(entity.getType()));
            player.sendPacket(new CustomPayloadS2CPacket(PolymerPacketIds.ENTITY_ID, buf));
        }
    }


        private static void sendSync(ServerPlayNetworkHandler handler, Identifier id, List<BufferWritable> entries) {
        var buf = buf();

        buf.writeVarInt(entries.size());

        for (var entry : entries) {
            entry.write(buf, handler);
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
