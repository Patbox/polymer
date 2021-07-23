package eu.pb4.polymer.other.client;

import net.minecraft.client.MinecraftClient;

public class ClientUtils {
    public static final String PACK_ID = "$polymer-resources";

    public static boolean isResourcePackLoaded() {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledNames().contains(PACK_ID);
    }
}
