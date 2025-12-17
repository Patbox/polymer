package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.core.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.core.impl.interfaces.PolymerGamePacketListenerExtension;
import eu.pb4.polymer.core.mixin.block.packet.ClientboundBlockUpdatePacketAccessor;
import eu.pb4.polymer.core.mixin.block.packet.ClientboundSectionBlocksUpdatePacketAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import xyz.nucleoid.packettweaker.PacketContext;

public class BlockPacketUtil {
    public static void sendFromPacket(Packet<?> packet, ServerGamePacketListenerImpl handler) {
        if (packet instanceof ClientboundBlockUpdatePacket blockUpdatePacket) {
            BlockState blockState = ((ClientboundBlockUpdatePacketAccessor) blockUpdatePacket).polymer$getState();
            if (PolymerImplUtils.POLYMER_STATES.contains(blockState)) {
                PolymerGamePacketListenerExtension.of(handler).polymer$delayAfterSequence(new SendSingleBlockInfo(handler, blockUpdatePacket.getPos(), blockState));
            }
        } else if (packet instanceof ClientboundLevelChunkWithLightPacket) {
            LevelChunk wc = ((ChunkDataS2CPacketInterface) packet).polymer$getWorldChunk();
            PolymerBlockPosStorage wci = (PolymerBlockPosStorage) wc;
            if (wc != null && wci.polymer$hasAny()) {
                PolymerServerProtocol.sendSectionUpdate(handler, wc);
                var ctx = PacketContext.create(handler);
                var iterator = wci.polymer$iterator();
                while (iterator.hasNext()) {
                    var pos = iterator.next();
                    var blockState = wc.getBlockState(pos);
                    if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
                        polymerBlock.onPolymerBlockSend(blockState, pos, ctx);
                    }
                }
            }
        } else if (packet instanceof ClientboundSectionBlocksUpdatePacket) {
            var chunk = (ClientboundSectionBlocksUpdatePacketAccessor) packet;

            PolymerGamePacketListenerExtension.of(handler).polymer$delayAfterSequence(new SendSequanceBlockInfo(handler,
                    chunk.polymer_getSectionPos(), chunk.polymer_getBlockStates(), chunk.polymer_getPositions()));
        }
    }

    public static void splitChunkDelta(ServerGamePacketListenerImpl handler, ClientboundSectionBlocksUpdatePacket cPacket) {
        cPacket.runUpdates((blockPos, blockState) -> handler.send(new ClientboundBlockUpdatePacket(blockPos.immutable(), blockState)));
    }

    public static void sendUpdate(ServerPlayer player, BlockPos pos) {
        var state = player.level().getBlockState(pos);
        player.connection.send(new ClientboundBlockUpdatePacket(pos, state));

        if (state.hasBlockEntity()) {
            var be = player.level().getBlockEntity(pos);
            if (be != null) {
                player.connection.send(ClientboundBlockEntityDataPacket.create(be));
            }
        }
    }

    private record SendSingleBlockInfo(ServerGamePacketListenerImpl handler, BlockPos pos, BlockState blockState) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendBlockUpdate(handler, pos, blockState);
            if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
                polymerBlock.onPolymerBlockSend(blockState, pos.mutable(), PacketContext.create(handler));
            }
        }
    }

    private record SendSequanceBlockInfo(ServerGamePacketListenerImpl handler, SectionPos chunkPos,
                                         BlockState[] blockStates, short[] localPos) implements Runnable {
        @Override
        public void run() {
            PolymerServerProtocol.sendMultiBlockUpdate(handler, chunkPos, localPos, blockStates);

            var blockPos = new BlockPos.MutableBlockPos();
            var ctx = PacketContext.create(handler);


            for (int i = 0; i < localPos.length; i++) {
                BlockState blockState = blockStates[i];
                blockPos.set(chunkPos.relativeToBlockX(localPos[i]), chunkPos.relativeToBlockY(localPos[i]), chunkPos.relativeToBlockZ(localPos[i]));


                if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock) {
                    polymerBlock.onPolymerBlockSend(blockState, blockPos, ctx);
                }
            }
        }
    }
}
