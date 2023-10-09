package eu.pb4.polymer.core.impl.compat;

import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import qouteall.imm_ptl.core.ducks.IECustomPayloadPacket;
import qouteall.imm_ptl.core.network.PacketRedirection;
import qouteall.q_misc_util.dimension.DimensionIdRecord;

import java.util.Objects;

public class ImmersivePortalsUtils {
    public static void sendBlockPackets(ServerPlayNetworkHandler handler, Packet<?> packet) {
        if (packet instanceof CustomPayloadS2CPacket payloadS2CPacket &&  payloadS2CPacket.payload() instanceof PacketRedirection.Payload payload) {
            PacketRedirection.withForceRedirect(Objects.requireNonNull(handler.player.getServer().getWorld(DimensionIdRecord.serverRecord.getDim(payload.dimensionIntId()))), () -> {
                BlockPacketUtil.sendFromPacket(payload.packet(), handler);
            });
        } else {
            BlockPacketUtil.sendFromPacket(packet, handler);
        }
    }
}
