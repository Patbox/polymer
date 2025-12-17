package eu.pb4.polymer.common.impl.compat;

import com.mojang.authlib.GameProfile;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public class FloodGateUtils {
    public static boolean isPlayerBroken(ServerPlayer player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUUID());
    }

    public static boolean isPlayerBroken(UUID uuid) {
        return FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }
}
