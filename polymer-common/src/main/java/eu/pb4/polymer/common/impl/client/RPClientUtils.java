package eu.pb4.polymer.common.impl.client;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class RPClientUtils {
    public static final String PACK_ID = "$polymer-resources";

    public static boolean isResourcePackLoaded() {
        return MinecraftClient.getInstance().getResourcePackManager().getEnabledNames().contains(PACK_ID);
    }
}
