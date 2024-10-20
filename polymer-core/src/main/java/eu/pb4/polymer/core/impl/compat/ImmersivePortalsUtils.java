package eu.pb4.polymer.core.impl.compat;

import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
import qouteall.imm_ptl.core.network.PacketRedirection;

import java.util.List;
import java.util.Objects;

public class ImmersivePortalsUtils {
    public static void sendBlockPackets(ServerPlayNetworkHandler handler, Packet<?> packet) {
        if (packet instanceof CustomPayloadS2CPacket payloadS2CPacket &&  payloadS2CPacket.payload() instanceof PacketRedirection.Payload payload) {
            PacketRedirection.withForceRedirect(Objects.requireNonNull(
                    handler.player.getServer().getWorld(PortalAPI.serverIntToDimKey(handler.getPlayer().server, payload.dimensionIntId()))), () -> {
                BlockPacketUtil.sendFromPacket(payload.packet(), handler);
            });
        } else {
            BlockPacketUtil.sendFromPacket(packet, handler);
        }
    }

    public static List<ServerPlayerEntity> getPlayerTracking(WorldChunk chunk) {
        return ImmPtlChunkTracking.getPlayersViewingChunk(chunk.getWorld().getRegistryKey(), chunk.getPos().x, chunk.getPos().z, false);
    }

    public static List<ServerPlayerEntity> getPlayerTracking(RegistryKey<World> worldRegistryKey, ChunkPos pos) {
        return ImmPtlChunkTracking.getPlayersViewingChunk(worldRegistryKey, pos.x, pos.z, false);
    }
}
