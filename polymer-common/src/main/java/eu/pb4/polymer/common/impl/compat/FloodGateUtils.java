package eu.pb4.polymer.common.impl.compat;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.network.ServerPlayerEntity;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public class FloodGateUtils {
    public static boolean isPlayerBroken(ServerPlayerEntity player) {
        return FloodgateApi.getInstance().isFloodgatePlayer(player.getUuid());
    }

    public static boolean isPlayerBroken(GameProfile profile) {
        return FloodgateApi.getInstance().isFloodgatePlayer(profile.getId());
    }
}
