package eu.pb4.polymer.virtualentity.impl.compat;

import org.jetbrains.annotations.ApiStatus;
//import qouteall.imm_ptl.core.chunk_loading.ImmPtlChunkTracking;
//import qouteall.imm_ptl.core.network.PacketRedirection;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;


@ApiStatus.Internal
public class ImmersivePortalsUtils {
    public static boolean isPlayerTracking(ServerPlayer player, LevelChunk chunk) {
        //return ImmPtlChunkTracking.isPlayerWatchingChunk(player, chunk.getLevel().dimension(), chunk.getPos().x, chunk.getPos().z);
        return false;
    }

    public static List<ServerPlayer> getPlayerTracking(LevelChunk chunk) {
        //return ImmPtlChunkTracking.getPlayersViewingChunk(chunk.getLevel().dimension(), chunk.getPos().x, chunk.getPos().z, false);
        return null;
    }

    public static void callRedirected(ServerLevel world, Runnable runnable) {
        //PacketRedirection.withForceRedirect(world, runnable);
        runnable.run();
    }
}
