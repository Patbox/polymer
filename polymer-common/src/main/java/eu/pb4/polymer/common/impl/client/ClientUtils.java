package eu.pb4.polymer.common.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static final String PACK_ID = "$polymer-resources";
    public static volatile ServerPlayerEntity backupPlayer;

    public static boolean isResourcePackLoaded() {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledNames().contains(PACK_ID);
    }

    public static boolean isSingleplayer() {
        return MinecraftClient.getInstance().getServer() != null;
    }

    public static ServerPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().player != null
                ? MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid())
                : backupPlayer;
    }

    public static boolean isClientThread() {
        return MinecraftClient.getInstance().isOnThread();
    }
}
