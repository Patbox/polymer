package eu.pb4.polymer.ext.client.api;

import eu.pb4.polymer.ext.client.impl.CEServerProtocol;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.Objects;

public class PolymerClientExtensions {
    public static void setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, ReloadLogoOverride override, String base64Texture) {
        setReloadScreen(handler, color, loadingBarColor, color, loadingBarColor, override, base64Texture);
    }
    public static void setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, int colorDark, int loadingBarColorDark, ReloadLogoOverride override, String base64Texture) {
        Objects.requireNonNull(base64Texture);
        Objects.requireNonNull(override);
        CEServerProtocol.sendSetReloadLogo(handler, color, loadingBarColor, colorDark, loadingBarColorDark, override, base64Texture);
    }

    public static void setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, int colorDark, int loadingBarColorDark, ReloadLogoOverride override) {
        Objects.requireNonNull(override);
        if (override.requiresImage()) {
            throw new IllegalArgumentException("Provided reload override requires image!");
        }
        CEServerProtocol.sendSetReloadLogo(handler, color, loadingBarColor, colorDark, loadingBarColorDark, override, null);
    }

    public static void clearReloadScreen(ServerPlayNetworkHandler handler) {
        CEServerProtocol.sendSetReloadLogo(handler, 0, 0, 0, 0, null, null);
    }


    public enum ReloadLogoOverride {
        DEFAULT,
        ICON,
        FULL_SCREEN,
        NONE;

        public boolean requiresImage() {
            return this == ICON || this == FULL_SCREEN;
        }
    }
}
