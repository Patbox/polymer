package eu.pb4.polymer.autohost.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public interface ResourcePackDataProvider {
    String getAddress();
    String getHash();
    boolean isReady();
    JsonElement saveSettings();
    void loadSettings(JsonElement settings);
    void serverStarted(MinecraftServer server);
    void serverStopped(MinecraftServer server);

    static ResourcePackDataProvider getActive() {
        return AutoHost.provider;
    }

    static <T> void register(Identifier identifier, Supplier<ResourcePackDataProvider> providerCreator) {
        AutoHost.TYPES.put(identifier, providerCreator);
    };

    default MinecraftServer.ServerResourcePackProperties getProperties() {
        return new MinecraftServer.ServerResourcePackProperties(this.getAddress(), this.getHash(), AutoHost.config.require || PolymerResourcePackUtils.isRequired(), AutoHost.message);
    }
}
