package eu.pb4.polymer.common.impl.compat;

import net.minecraft.server.network.ServerPlayerEntity;
import org.geysermc.floodgate.api.FloodgateApi;

public class FloodGateUtils {
    public static boolean isPlayerBroken(ServerPlayerEntity player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUuid());
    }
}
