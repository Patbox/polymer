package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.mixin.block.packet.BlockUpdateS2CPacketAccessor;
import eu.pb4.polymer.mixin.block.packet.ChunkDeltaUpdateS2CPacketAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;

public class BlockPacketUtil {
    public static void sendFromPacket(Packet<?> packet, ServerPlayNetworkHandler handler) {
        if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
            BlockState blockState = ((BlockUpdateS2CPacketAccessor) blockUpdatePacket).polymer_getState();
            BlockPos pos = blockUpdatePacket.getPos();
            PolymerServerProtocol.sendBlockUpdate(handler, pos, blockState);

            if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                polymerBlock.onPolymerBlockSend(handler.player, pos.mutableCopy(), blockState);
            }

        } else if (packet instanceof ChunkDataS2CPacket) {
            WorldChunk wc = ((ChunkDataS2CPacketInterface) packet).polymer_getWorldChunk();
            PolymerBlockPosStorage wci = (PolymerBlockPosStorage) wc;
            if (wc != null) {
                PolymerServerProtocol.sendSectionUpdate(handler, wc);

                var iterator = wci.polymer_iterator();
                while (iterator.hasNext()) {
                    var pos = iterator.next();
                    BlockState blockState = wc.getBlockState(pos);
                    if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                        polymerBlock.onPolymerBlockSend(handler.player, pos, blockState);
                    }
                }
            }
        } else if (packet instanceof ChunkDeltaUpdateS2CPacket) {
            ChunkDeltaUpdateS2CPacketAccessor chunk = (ChunkDeltaUpdateS2CPacketAccessor) packet;
            ChunkSectionPos chunkPos = chunk.polymer_getSectionPos();
            BlockState[] blockStates = chunk.polymer_getBlockStates();
            short[] localPos = chunk.polymer_getPositions();
            PolymerServerProtocol.sendMultiBlockUpdate(handler, chunkPos, localPos, blockStates);

            var blockPos = new BlockPos.Mutable();
            for (int i = 0; i < localPos.length; i++) {
                BlockState blockState = blockStates[i];

                if (blockState.getBlock() instanceof PolymerBlock) {

                    blockPos.set(chunkPos.unpackBlockX(localPos[i]), chunkPos.unpackBlockY(localPos[i]), chunkPos.unpackBlockZ(localPos[i]));
                    ((PolymerBlock) blockState.getBlock()).onPolymerBlockSend(handler.player, blockPos, blockState);
                }
            }
        }
    }

    public static void splitChunkDelta(ServerPlayNetworkHandler handler, ChunkDeltaUpdateS2CPacket cPacket) {
        cPacket.visitUpdates((blockPos, blockState) -> handler.sendPacket(new BlockUpdateS2CPacket(blockPos.toImmutable(), blockState)));
    }
}
