package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.mixin.block.packet.BlockUpdateS2CPacketAccessor;
import eu.pb4.polymer.core.mixin.block.packet.ChunkDeltaUpdateS2CPacketAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import xyz.nucleoid.packettweaker.PacketContext;

public class BlockPacketUtil {
    public static void sendFromPacket(Packet<?> packet, ServerPlayNetworkHandler handler) {
        if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
            BlockState blockState = ((BlockUpdateS2CPacketAccessor) blockUpdatePacket).polymer$getState();
            if (PolymerImplUtils.POLYMER_STATES.contains(blockState)) {
                PolymerPlayNetworkHandlerExtension.of(handler).polymer$delayAfterSequence(new SendSingleBlockInfo(handler, blockUpdatePacket.getPos(), blockState));
            }
        } else if (packet instanceof ChunkDataS2CPacket) {
            WorldChunk wc = ((ChunkDataS2CPacketInterface) packet).polymer$getWorldChunk();
            PolymerBlockPosStorage wci = (PolymerBlockPosStorage) wc;
            if (wc != null && wci.polymer$hasAny()) {
                PolymerServerProtocol.sendSectionUpdate(handler, wc);
                var ctx = PacketContext.of(handler);
                var iterator = wci.polymer$iterator();
                while (iterator.hasNext()) {
                    var pos = iterator.next();
                    var blockState = wc.getBlockState(pos);
                    if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                        polymerBlock.onPolymerBlockSend(blockState, pos, ctx);
                    }
                }
            }
        } else if (packet instanceof ChunkDeltaUpdateS2CPacket) {
            var chunk = (ChunkDeltaUpdateS2CPacketAccessor) packet;

            PolymerPlayNetworkHandlerExtension.of(handler).polymer$delayAfterSequence(new SendSequanceBlockInfo(handler,
                    chunk.polymer_getSectionPos(), chunk.polymer_getBlockStates(), chunk.polymer_getPositions()));
        }
    }

    public static void splitChunkDelta(ServerPlayNetworkHandler handler, ChunkDeltaUpdateS2CPacket cPacket) {
        cPacket.visitUpdates((blockPos, blockState) -> handler.sendPacket(new BlockUpdateS2CPacket(blockPos.toImmutable(), blockState)));
    }

    private record SendSingleBlockInfo(ServerPlayNetworkHandler handler, BlockPos pos, BlockState blockState) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendBlockUpdate(handler, pos, blockState);
            if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                polymerBlock.onPolymerBlockSend(blockState, pos.mutableCopy(), PacketContext.of(handler));
            }
        }
    }

    private record SendSequanceBlockInfo(ServerPlayNetworkHandler handler, ChunkSectionPos chunkPos,
                                         BlockState[] blockStates, short[] localPos) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendMultiBlockUpdate(handler, chunkPos, localPos, blockStates);

            var blockPos = new BlockPos.Mutable();
            var ctx = PacketContext.of(handler);


            for (int i = 0; i < localPos.length; i++) {
                BlockState blockState = blockStates[i];
                blockPos.set(chunkPos.unpackBlockX(localPos[i]), chunkPos.unpackBlockY(localPos[i]), chunkPos.unpackBlockZ(localPos[i]));

                if (blockState.getBlock() instanceof PolymerBlock) {
                    ((PolymerBlock) blockState.getBlock()).onPolymerBlockSend(blockState, blockPos, ctx);
                }
            }
        }
    }
}
