package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import net.minecraft.server.MinecraftServer;

public record EmptyProvider() implements ResourcePackDataProvider {
    public static ResourcePackDataProvider INSTANCE = new EmptyProvider();

    @Override
    public String getAddress() {
        return "";
    }

    @Override
    public String getHash() {
        return "";
    }

    @Override
    public boolean isReady() {
        return false;
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
}
