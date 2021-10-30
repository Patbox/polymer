package eu.pb4.polymer.impl.client.networking;

import eu.pb4.polymer.api.client.block.ClientPolymerBlock;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.world.ClientBlockStorageInterface;
import eu.pb4.polymer.impl.client.world.ClientPolymerBlocks;
import eu.pb4.polymer.impl.networking.PolymerPacketIds;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockEntry;
import eu.pb4.polymer.impl.networking.packets.PolymerBlockStateEntry;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;

public class ClientPacketHandler {
    public static void handle(ClientPlayNetworkHandler handler, Identifier identifier, PacketByteBuf buf) {
        try {
            switch (identifier.getPath()) {
                case PolymerPacketIds.REGISTRY_BLOCK -> {
                    var size = buf.readVarInt();

                    for (int i = 0; i < size; i++) {
                        var entry = PolymerBlockEntry.read(buf);
                        ClientPolymerBlocks.BLOCKS.set(new ClientPolymerBlock(entry.identifier(), entry.numId(), entry.text(), entry.visual()), entry.numId());
                    }
                }

                case PolymerPacketIds.REGISTRY_BLOCKSTATE -> {
                    var size = buf.readVarInt();

                    for (int i = 0; i < size; i++) {
                        var entry = PolymerBlockStateEntry.read(buf);
                        ClientPolymerBlocks.BLOCKSTATES.set(new ClientPolymerBlock.State(entry.states(), ClientPolymerBlocks.BLOCKS.get(entry.blockId())), entry.numId());
                    }
                }

                case PolymerPacketIds.BLOCK_UPDATE -> {
                    var pos = buf.readBlockPos();
                    var id = buf.readVarInt();
                    var block = ClientPolymerBlocks.BLOCKSTATES.get(id);

                    var chunk = handler.getWorld().getChunk(pos);

                    if (chunk instanceof ClientBlockStorageInterface storage) {
                        storage.polymer_setClientPolymerBlock(pos.getX(), pos.getY(), pos.getZ(), block);
                    }
                }

                case PolymerPacketIds.CHUNK_SECTION_UPDATE -> {
                    var sectionPos = buf.readChunkSectionPos();
                    var size = buf.readVarInt();
                    var section = handler.getWorld().getChunk(sectionPos.getX(), sectionPos.getZ()).getSection(sectionPos.getY());

                    if (section instanceof ClientBlockStorageInterface storage) {
                        for (int i = 0; i < size; i++) {
                            long value = buf.readVarLong();
                            var pos = (short) ((int) (value & 4095L));
                            var block = ClientPolymerBlocks.BLOCKSTATES.get((int) (value >>> 12));

                            storage.polymer_setClientPolymerBlock(ChunkSectionPos.unpackLocalX(pos), ChunkSectionPos.unpackLocalY(pos), ChunkSectionPos.unpackLocalZ(pos), block);
                        }
                    }
                }
            }
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Invalid " + identifier + " packet received from server!");
            PolymerMod.LOGGER.error(e);
        }
    }
}
