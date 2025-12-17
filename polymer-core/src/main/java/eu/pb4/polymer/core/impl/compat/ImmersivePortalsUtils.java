package eu.pb4.polymer.core.impl.compat;

import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import qouteall.imm_ptl.core.api.PortalAPI;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
import qouteall.imm_ptl.core.network.PacketRedirection;

import java.util.List;
import java.util.Objects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

public class ImmersivePortalsUtils {
    public static void sendBlockPackets(ServerGamePacketListenerImpl handler, Packet<?> packet) {
        if (packet instanceof ClientboundCustomPayloadPacket payloadS2CPacket &&  payloadS2CPacket.payload() instanceof PacketRedirection.Payload payload) {
            PacketRedirection.withForceRedirect(Objects.requireNonNull(
                    handler.player.level().getServer().getLevel(PortalAPI.serverIntToDimKey(handler.getPlayer().level().getServer(), payload.dimensionIntId()))), () -> {
                BlockPacketUtil.sendFromPacket(payload.packet(), handler);
            });
        } else {
            BlockPacketUtil.sendFromPacket(packet, handler);
        }
    }

    public static List<ServerPlayer> getPlayerTracking(LevelChunk chunk) {
        return ImmPtlChunkTracking.getPlayersViewingChunk(chunk.getLevel().dimension(), chunk.getPos().x, chunk.getPos().z, false);
    }

    public static List<ServerPlayer> getPlayerTracking(ResourceKey<Level> worldRegistryKey, ChunkPos pos) {
        return ImmPtlChunkTracking.getPlayersViewingChunk(worldRegistryKey, pos.x, pos.z, false);
    }
}
