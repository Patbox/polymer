package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import eu.pb4.polymer.impl.networking.BlockPacketUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import qouteall.imm_ptl.core.network.IPCommonNetwork;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ip_ServerPlayNetworkHandlerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Shadow
    public abstract void sendPacket(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener);

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
    private Packet<?> polymer_ip_catchAndReplace(Packet<?> packet) {
        if (packet instanceof IPAttachedPacket attachedPacket && !attachedPacket.polymer_ip_shouldSkip()
                && attachedPacket.polymer_ip_getAttachedPacket() != null && attachedPacket.polymer_ip_getAttachedDimension() != null
        ) {
            PolymerImplUtils.setPlayer(this.player);
            var newPacket = IPNetworking.createRedirectedMessage(attachedPacket.polymer_ip_getAttachedDimension(), attachedPacket.polymer_ip_getAttachedPacket());
            ((IPAttachedPacket) newPacket).polymer_ip_setSkip(true);
            PolymerImplUtils.setPlayer(null);
            return newPacket;
        }

        return packet;
    }

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private Packet<?> polymer_patchPackets(Packet<?> packet) {
        if (packet instanceof IPAttachedPacket attachedPacket && attachedPacket.polymer_ip_getAttachedDimension() != null) {
            IPCommonNetwork.withForceRedirect(this.player.getServer().getWorld(attachedPacket.polymer_ip_getAttachedDimension()), () -> {
                PolymerImplUtils.setPlayer(this.getPlayer());
                BlockPacketUtil.sendFromPacket(attachedPacket.polymer_ip_getAttachedPacket(), (ServerPlayNetworkHandler) (Object) this);
                PolymerImplUtils.setPlayer(null);
            });
        }
        return packet;
    }
}
