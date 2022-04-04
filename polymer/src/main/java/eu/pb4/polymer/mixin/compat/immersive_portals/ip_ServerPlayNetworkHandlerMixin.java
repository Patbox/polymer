package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import eu.pb4.polymer.impl.interfaces.PlayerAwarePacket;
import eu.pb4.polymer.impl.networking.BlockInfoUtil;
import eu.pb4.polymer.mixin.block.packet.BlockUpdateS2CPacketAccessor;
import eu.pb4.polymer.mixin.block.packet.ChunkDeltaUpdateS2CPacketAccessor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import qouteall.imm_ptl.core.network.IPCommonNetwork;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ip_ServerPlayNetworkHandlerMixin {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Shadow public ServerPlayerEntity player;

    @Shadow public abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener);

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void polymer_ip_catchAndReplace(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if (packet instanceof IPAttachedPacket attachedPacket && !attachedPacket.polymer_ip_shouldSkip() && attachedPacket.polymer_ip_getAttachedPacket() != null && attachedPacket.polymer_ip_getAttachedDimension() != null) {
            var realPacket = attachedPacket.polymer_ip_getAttachedPacket();
            if ((realPacket instanceof BlockUpdateS2CPacket blockUpdate
                    && ((BlockUpdateS2CPacketAccessor) blockUpdate).polymer_getState().getBlock() instanceof PolymerBlock)
                    || (realPacket instanceof ChunkDeltaUpdateS2CPacket blockUpdate2 && this.polymer_op_containsPlayerAware(blockUpdate2))
            ) {
                PolymerImplUtils.setPlayer(this.getPlayer());
                var newPacket = IPNetworking.createRedirectedMessage(attachedPacket.polymer_ip_getAttachedDimension(), realPacket);
                ((IPAttachedPacket) newPacket).polymer_ip_setSkip(true);
                this.sendPacket(newPacket, listener);
                PolymerImplUtils.setPlayer(null);
                ci.cancel();
            }
        }
    }

    @Unique
    private final boolean polymer_op_containsPlayerAware(ChunkDeltaUpdateS2CPacket states) {
        for (var i : ((ChunkDeltaUpdateS2CPacketAccessor) states).polymer_getBlockStates()) {
            if (i.getBlock() instanceof PolymerBlock) {
                return true;
            }
        }
        return false;
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private Packet<?> polymer_patchPackets(Packet<?> packet) {
        if (packet instanceof IPAttachedPacket attachedPacket && attachedPacket.polymer_ip_getAttachedPacket() instanceof PlayerAwarePacket && attachedPacket.polymer_ip_getAttachedDimension() != null) {
            IPCommonNetwork.withForceRedirect(this.player.getServer().getWorld(attachedPacket.polymer_ip_getAttachedDimension()), () -> {
                PolymerImplUtils.setPlayer(this.getPlayer());
                BlockInfoUtil.sendFromPacket(attachedPacket.polymer_ip_getAttachedPacket(), (ServerPlayNetworkHandler) (Object) this);
                PolymerImplUtils.setPlayer(null);
            });
        }
        return packet;
    }
}
