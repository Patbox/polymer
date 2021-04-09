package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualHeadBlock;
import eu.pb4.polymer.interfaces.WorldChunkInterface;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.block.BlockState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow public abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener);

    @Shadow public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private void catchBlockUpdates(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo cb) {
        if (packet instanceof BlockUpdateS2CPacket) {
            BlockUpdateS2CPacketAccessor b = (BlockUpdateS2CPacketAccessor) packet;
            BlockState blockState = b.getState();

            if (blockState.getBlock() instanceof VirtualHeadBlock) {
                BlockPos pos = ((BlockUpdateS2CPacket) packet).getPos();

                this.sendPacket(((VirtualHeadBlock) blockState.getBlock()).getVirtualHeadPacket(blockState, pos), listener);
            }
        } else if (packet instanceof ChunkDataS2CPacket) {
            ChunkDataS2CPacketAccessor c = (ChunkDataS2CPacketAccessor) packet;
            WorldChunk wc = this.player.world.getChunk(c.getChunkX(), c.getChunkZ());
            WorldChunkInterface wci = (WorldChunkInterface) wc;
            if (wc != null) {
                for (BlockPos pos : wci.getVirtualHeadBlocks()) {
                    BlockState blockState = wc.getBlockState(pos);

                    this.sendPacket(((VirtualHeadBlock) blockState.getBlock()).getVirtualHeadPacket(blockState, pos), listener);
                }
            }
        }
    }
}
