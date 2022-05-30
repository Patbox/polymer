package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import eu.pb4.polymer.impl.networking.BlockPacketUtil;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import qouteall.imm_ptl.core.network.IPCommonNetwork;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ip_ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("TAIL"))
    private Packet<?> polymer_sendRequiredPackets(Packet<?> packet) {
        if (packet instanceof IPAttachedPacket attachedPacket && attachedPacket.polymer_ip_getAttachedWorld() != null && attachedPacket.polymer_ip_getAttachedPacket() != null) {
            IPCommonNetwork.withForceRedirect(this.player.getServer().getWorld(attachedPacket.polymer_ip_getAttachedWorld()), () -> {
                PolymerImplUtils.setPlayer(this.player);
                BlockPacketUtil.sendFromPacket(attachedPacket.polymer_ip_getAttachedPacket(), (ServerPlayNetworkHandler) (Object) this);
                PolymerImplUtils.setPlayer(null);
            });
        }
        return packet;
    }
}
