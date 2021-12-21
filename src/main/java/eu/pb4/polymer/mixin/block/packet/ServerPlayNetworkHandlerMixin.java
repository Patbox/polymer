package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.impl.interfaces.ChunkDataS2CPacketInterface;
import eu.pb4.polymer.impl.interfaces.PolymerBlockPosStorage;
import eu.pb4.polymer.impl.networking.BlockInfoUtil;
import eu.pb4.polymer.impl.networking.PolymerServerProtocol;
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
            BlockInfoUtil.sendFromPacket(packet, (ServerPlayNetworkHandler) (Object) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
