package eu.pb4.polymer.core.impl.compat;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import qouteall.imm_ptl.core.ducks.IECustomPayloadPacket;
import qouteall.imm_ptl.core.network.PacketRedirection;

public class ImmersivePortalsUtils {
    public static void sendBlockPackets(ServerPlayNetworkHandler handler, Packet<?> packet) {
        if (packet instanceof IECustomPayloadPacket attachedPacket && attachedPacket.ip_getRedirectedPacket() != null && attachedPacket.ip_getRedirectedDimension() != null) {
            PolymerCommonUtils.executeWithPlayerContext(handler.player, () -> {
                PacketRedirection.withForceRedirect(handler.player.getServer().getWorld(attachedPacket.ip_getRedirectedDimension()), () -> {
                    BlockPacketUtil.sendFromPacket(attachedPacket.ip_getRedirectedPacket(), handler);
                });
            });
        }
    }
}
