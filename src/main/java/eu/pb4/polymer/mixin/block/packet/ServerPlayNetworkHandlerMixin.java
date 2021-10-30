package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.impl.networking.ServerPacketBuilders;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private void polymer_catchBlockUpdates(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo cb) {
        try {
            if (packet instanceof BlockUpdateS2CPacket blockUpdatePacket) {
                BlockState blockState = ((BlockUpdateS2CPacketAccessor) blockUpdatePacket).polymer_getState();
                BlockPos pos = blockUpdatePacket.getPos();

                if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                    polymerBlock.onPolymerBlockSend(this.player, pos.mutableCopy(), blockState);
                }

                ServerPacketBuilders.createSingleBlockPacket((ServerPlayNetworkHandler) (Object) this, pos, blockState);
            } else if (packet instanceof ChunkDataS2CPacket chunkDataS2CPacket) {
                WorldChunk wc = ((ChunkDataS2CPacketInterface) packet).polymer_getWorldChunk();
                PolymerBlockPosStorage wci = (PolymerBlockPosStorage) wc;
                if (wc != null) {
                    var iterator = wci.polymer_iterator();
                    while (iterator.hasNext()) {
                        var pos = iterator.next();
                        BlockState blockState = wc.getBlockState(pos);
                        if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
                            polymerBlock.onPolymerBlockSend(this.player, pos, blockState);
                        }
                    }
                    ServerPacketBuilders.createChunkPacket((ServerPlayNetworkHandler) (Object) this, chunkDataS2CPacket, wc);
                }
            } else if (packet instanceof ChunkDeltaUpdateS2CPacket) {
                ChunkDeltaUpdateS2CPacketAccessor chunk = (ChunkDeltaUpdateS2CPacketAccessor) packet;
                ChunkSectionPos chunkPos = chunk.polymer_getSectionPos();
                BlockState[] blockStates = chunk.polymer_getBlockStates();
                short[] localPos = chunk.polymer_getPositions();

                var blockPos = new BlockPos.Mutable();
                for (int i = 0; i < localPos.length; i++) {
                    BlockState blockState = blockStates[i];


                    if (blockState.getBlock() instanceof PolymerBlock) {

                        blockPos.set(chunkPos.unpackBlockX(localPos[i]), chunkPos.unpackBlockY(localPos[i]), chunkPos.unpackBlockZ(localPos[i]));
                        ((PolymerBlock) blockState.getBlock()).onPolymerBlockSend(this.player, blockPos, blockState);
                    }
                }

                ServerPacketBuilders.createMultiBlockPacket((ServerPlayNetworkHandler) (Object) this, chunkPos, localPos, blockStates);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
