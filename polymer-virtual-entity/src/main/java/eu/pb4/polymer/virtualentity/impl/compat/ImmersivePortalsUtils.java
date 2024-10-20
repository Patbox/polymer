package eu.pb4.polymer.virtualentity.impl.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
import qouteall.imm_ptl.core.network.PacketRedirection;

import java.util.List;


@ApiStatus.Internal
public class ImmersivePortalsUtils {
    public static boolean isPlayerTracking(ServerPlayerEntity player, WorldChunk chunk) {
        return ImmPtlChunkTracking.isPlayerWatchingChunk(player, chunk.getWorld().getRegistryKey(), chunk.getPos().x, chunk.getPos().z);
    }

    public static List<ServerPlayerEntity> getPlayerTracking(WorldChunk chunk) {
        return ImmPtlChunkTracking.getPlayersViewingChunk(chunk.getWorld().getRegistryKey(), chunk.getPos().x, chunk.getPos().z, false);
    }

    public static void callRedirected(ServerWorld world, Runnable runnable) {
        PacketRedirection.withForceRedirect(world, runnable);
    }
}
