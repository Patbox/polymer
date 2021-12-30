package eu.pb4.polymer.ext.client.api;

import eu.pb4.polymer.ext.client.impl.CEServerProtocol;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.Objects;

public class PolymerClientExtensions {
    public static boolean setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, ReloadLogoOverride override, byte[] texture) {
        return setReloadScreen(handler, color, loadingBarColor, color, loadingBarColor, override, texture);
    }

    public static boolean setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, int colorDark, int loadingBarColorDark, ReloadLogoOverride override, byte[] texture) {
        Objects.requireNonNull(texture);
        Objects.requireNonNull(override);
        return CEServerProtocol.sendSetReload(handler, color, loadingBarColor, colorDark, loadingBarColorDark, override, texture);
    }

    public static boolean setReloadScreen(ServerPlayNetworkHandler handler, int color, int loadingBarColor, int colorDark, int loadingBarColorDark, ReloadLogoOverride override) {
        Objects.requireNonNull(override);
        if (override.requiresImage()) {
            throw new IllegalArgumentException("Provided reload override requires image!");
        }
        return CEServerProtocol.sendSetReload(handler, color, loadingBarColor, colorDark, loadingBarColorDark, override, null);
    }

    public static boolean clearReloadScreen(ServerPlayNetworkHandler handler) {
        return CEServerProtocol.sendSetReload(handler, 0, 0, 0, 0, null, null);
    }

    public static boolean createToast(ServerPlayNetworkHandler handler, Text text, int time) {
        Objects.requireNonNull(text);
        return CEServerProtocol.sendToast(handler, text, time, null, null);
    }

    public static boolean createToast(ServerPlayNetworkHandler handler, Text text, ItemStack stack, int time) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(stack);
        return CEServerProtocol.sendToast(handler, text, time, stack, null);
    }

    public static boolean createToast(ServerPlayNetworkHandler handler, Text text, byte[] texture, int time) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(texture);
        return CEServerProtocol.sendToast(handler, text, time, null, texture);
    }

    public enum ReloadLogoOverride {
        DEFAULT,
        ICON,
        FULL_SCREEN,
        NONE,
        REMOVE_SCREEN;

        public boolean requiresImage() {
            return this == ICON || this == FULL_SCREEN;
        }
    }
}
