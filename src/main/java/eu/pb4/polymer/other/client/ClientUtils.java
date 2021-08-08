package eu.pb4.polymer.other.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

@Environment(EnvType.CLIENT)
public class ClientUtils {
    public static final String PACK_ID = "$polymer-resources";

    @Environment(EnvType.CLIENT)
    public static boolean isResourcePackLoaded() {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledNames().contains(PACK_ID);
    }

    public static boolean isSingleplayer() {
        return MinecraftClient.getInstance().getServer() != null;
    }

    public static ServerPlayerEntity getPlayer() {
        return isSingleplayer() ? MinecraftClient.getInstance().getServer().getPlayerManager().getPlayer(MinecraftClient.getInstance().player.getUuid()) : null;
    }
}
