package eu.pb4.polymer.autohost.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface ResourcePackDataProvider {
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
    default Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection) {
        return getProperties();
    };

    @Deprecated
    default Collection<MinecraftServer.ServerResourcePackProperties> getProperties() {
        return List.of();
    };

    static MinecraftServer.ServerResourcePackProperties createProperties(@Nullable UUID uuid, String address, @Nullable String hash) {
        return new MinecraftServer.ServerResourcePackProperties(uuid, address, hash, AutoHost.config.require || PolymerResourcePackUtils.isRequired(), AutoHost.message);
    }
}
