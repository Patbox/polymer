package eu.pb4.polymer.ext.client.impl.client;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import eu.pb4.polymer.ext.client.api.PolymerClientExtensions;
import eu.pb4.polymer.ext.client.impl.CEImplUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CERegistry {
    public final static Identifier RELOAD_LOGO_IDENTIFIER = CEImplUtils.id("reload_logo");
    public final static Identifier EMPTY_TEXTURE = CEImplUtils.id("empty");
    public static boolean customReloadLogo;
    public static int customReloadColor;
    public static int customReloadColorDark;
    public static PolymerClientExtensions.ReloadLogoOverride customReloadMode;
    public static int customReloadColorBar;
    public static int customReloadColorBarDark;
    public static int customReloadTextureHeight;
    public static int customReloadTextureWidth;


    public static void initialize() {
        PolymerClientUtils.ON_CLEAR.register(CERegistry::clear);
        PolymerClientUtils.ON_DISABLE.register(CERegistry::onDisable);
        InternalClientRegistry.TICK.register(CERegistry::tick);

        MinecraftClient.getInstance().execute(() -> {
            MinecraftClient.getInstance().getTextureManager().registerTexture(EMPTY_TEXTURE, new NativeImageBackedTexture(new NativeImage(512, 512, false)));
        });
    }

    private static void onDisable() {
        var client = MinecraftClient.getInstance();
        client.execute(() -> {
            clear();
            
            customReloadLogo = false;
            var texture = client.getTextureManager().getOrDefault(RELOAD_LOGO_IDENTIFIER, null);
            if (texture != null) {
                client.getTextureManager().destroyTexture(RELOAD_LOGO_IDENTIFIER);
                texture.close();
            }

            CEToastImpl.removeAll(client);
        });
    }

    public static void clear() {
        var client = MinecraftClient.getInstance();
        client.execute(() -> {

        });
    }

    public static void tick() {
        var client = MinecraftClient.getInstance();
        CEToastImpl.update(client);
    }
}
