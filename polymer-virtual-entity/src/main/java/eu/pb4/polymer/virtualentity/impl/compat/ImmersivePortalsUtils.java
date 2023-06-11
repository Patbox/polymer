package eu.pb4.polymer.virtualentity.impl.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;
import qouteall.imm_ptl.core.chunk_loading.NewChunkTrackingGraph;
import qouteall.imm_ptl.core.network.PacketRedirection;


@ApiStatus.Internal
public class ImmersivePortalsUtils {
    public static boolean isPlayerTracking(ServerPlayerEntity player, WorldChunk chunk) {
        return NewChunkTrackingGraph.isPlayerWatchingChunk(player, chunk.getWorld().getRegistryKey(), chunk.getPos().x, chunk.getPos().z);
    }

    public static void callRedirected(ServerWorld world, Runnable runnable) {
        PacketRedirection.withForceRedirect(world, runnable);
    }
}
