package eu.pb4.polymer.ext.client.impl.client;

import eu.pb4.polymer.api.client.PolymerClientUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.data.client.model.Texture;
import net.minecraft.util.Identifier;

public class CERegistry {
    public static Identifier RELOAD_LOGO_IDENTIFIER = new Identifier("polymer_client_ext", "reload_logo");
    public static Texture RELOAD_LOGO = null;


    public static void initialize() {
        PolymerClientUtils.ON_CLEAR.register(CERegistry::clear);
    }

    public static void clear() {
        var client = MinecraftClient.getInstance();
        RELOAD_LOGO = null;
        client.getTextureManager().destroyTexture(RELOAD_LOGO_IDENTIFIER);
    }
}
