package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.interfaces.WorldChunkInterface;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private void catchBlockUpdates(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo cb) {
        try {
            if (packet instanceof BlockUpdateS2CPacket) {
                BlockUpdateS2CPacketAccessor b = (BlockUpdateS2CPacketAccessor) packet;
                BlockState blockState = b.getStateServer();

                if (blockState.getBlock() instanceof VirtualBlock) {
                    BlockPos pos = ((BlockUpdateS2CPacketAccessor) packet).getPosServer();
                    ((VirtualBlock) blockState.getBlock()).sendPacketsAfterCreation(this.player, pos, blockState);
                }
            } else if (packet instanceof ChunkDataS2CPacket) {
                WorldChunk wc = ((ChunkDataS2CPacketInterface) packet).getWorldChunk();
                WorldChunkInterface wci = (WorldChunkInterface) wc;
                if (wc != null) {
                    for (BlockPos pos : wci.getVirtualBlocks()) {
                        BlockState blockState = wc.getBlockState(pos);
                        if (blockState.getBlock() instanceof VirtualBlock) {
                            ((VirtualBlock) blockState.getBlock()).sendPacketsAfterCreation(this.player, pos, blockState);
                        }
                    }
                }
            } else if (packet instanceof ChunkDeltaUpdateS2CPacket) {
                ChunkDeltaUpdateS2CPacketAccessor chunk = (ChunkDeltaUpdateS2CPacketAccessor) packet;
                ChunkSectionPos chunkPos = chunk.getSectionPosServer();
                BlockState[] blockStates = chunk.getBlockStatesServer();
                short[] localPos = chunk.getPositionsServer();

                for (int i = 0; i < localPos.length; i++) {
                    BlockState blockState = blockStates[i];

                    if (blockState.getBlock() instanceof VirtualBlock) {
                        BlockPos blockPos = chunkPos.unpackBlockPos(localPos[i]);
                        ((VirtualBlock) blockState.getBlock()).sendPacketsAfterCreation(this.player, blockPos, blockState);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
