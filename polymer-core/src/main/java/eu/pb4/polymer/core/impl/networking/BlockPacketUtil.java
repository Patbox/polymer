package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerPlayNetworkHandlerExtension;
import eu.pb4.polymer.core.mixin.block.packet.BlockUpdateS2CPacketAccessor;
import eu.pb4.polymer.core.mixin.block.packet.ChunkDeltaUpdateS2CPacketAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
                var ctx = PacketContext.create(handler);
                var iterator = wci.polymer$iterator();
                while (iterator.hasNext()) {
                    var pos = iterator.next();
                    var blockState = wc.getBlockState(pos);
                    if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
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

    public static void sendUpdate(ServerPlayerEntity player, BlockPos pos) {
        var state = player.getWorld().getBlockState(pos);
        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, state));

        if (state.hasBlockEntity()) {
            var be = player.getWorld().getBlockEntity(pos);
            if (be != null) {
                player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(be));
            }
        }
    }

    private record SendSingleBlockInfo(ServerPlayNetworkHandler handler, BlockPos pos, BlockState blockState) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendBlockUpdate(handler, pos, blockState);
            if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
                polymerBlock.onPolymerBlockSend(blockState, pos.mutableCopy(), PacketContext.create(handler));
            }
        }
    }

    private record SendSequanceBlockInfo(ServerPlayNetworkHandler handler, ChunkSectionPos chunkPos,
                                         BlockState[] blockStates, short[] localPos) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendMultiBlockUpdate(handler, chunkPos, localPos, blockStates);

            var blockPos = new BlockPos.Mutable();
            var ctx = PacketContext.create(handler);


            for (int i = 0; i < localPos.length; i++) {
                BlockState blockState = blockStates[i];
                blockPos.set(chunkPos.unpackBlockX(localPos[i]), chunkPos.unpackBlockY(localPos[i]), chunkPos.unpackBlockZ(localPos[i]));


                if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
                    polymerBlock.onPolymerBlockSend(blockState, blockPos, ctx);
                }
            }
        }
    }
}
