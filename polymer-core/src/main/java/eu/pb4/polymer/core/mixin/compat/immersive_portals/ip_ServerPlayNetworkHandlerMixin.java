package eu.pb4.polymer.core.mixin.compat.immersive_portals;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import qouteall.imm_ptl.core.ducks.IECustomPayloadPacket;
import qouteall.imm_ptl.core.network.PacketRedirection;

@Mixin(value = ServerPlayNetworkHandler.class)
public abstract class ip_ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("TAIL"))
    private Packet<?> polymer_sendRequiredPackets(Packet<?> packet) {
        if (packet instanceof IECustomPayloadPacket attachedPacket && attachedPacket.ip_getRedirectedPacket() != null && attachedPacket.ip_getRedirectedDimension() != null) {
            PolymerCommonUtils.executeWithPlayerContext(this.player, () -> {
                PacketRedirection.withForceRedirect(this.player.getServer().getWorld(attachedPacket.ip_getRedirectedDimension()), () -> {
                    BlockPacketUtil.sendFromPacket(attachedPacket.ip_getRedirectedPacket(), (ServerPlayNetworkHandler) (Object) this);
                });
            });
        }
        return packet;
    }
}
