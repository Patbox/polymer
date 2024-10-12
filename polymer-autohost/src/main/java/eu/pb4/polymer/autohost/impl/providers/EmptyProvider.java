package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record EmptyProvider() implements ResourcePackDataProvider {
    public static ResourcePackDataProvider INSTANCE = new EmptyProvider();

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public JsonElement saveSettings() {
        return JsonNull.INSTANCE;
    }

    @Override
    public void loadSettings(JsonElement settings) {

    }

    @Override
    public void serverStarted(MinecraftServer server) {

    }

    @Override
    public void serverStopped(MinecraftServer server) {

    }

    @Override
    public Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection) {
        return List.of();
    }
}
