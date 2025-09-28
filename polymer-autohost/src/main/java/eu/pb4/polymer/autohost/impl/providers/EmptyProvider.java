package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.api.AutoHostUtils;
import eu.pb4.polymer.autohost.api.ResourcePackDataProvider;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import xyz.nucleoid.packettweaker.PacketContext;

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
        try {
            PolymerResourcePackMod.generateAndCall(server, true, server::sendMessage, () -> {});
        } catch (Throwable e) {
            CommonImpl.LOGGER.warn("Failed to generate the resource pack!", e);
        }
    }

    @Override
    public void serverStopped(MinecraftServer server) {

    }

    @Override
    public Collection<MinecraftServer.ServerResourcePackProperties> getProperties(ClientConnection connection) {
        return List.of();
    }

    @Override
    public String getMainFilePath(PacketContext context) {
        return getFilePath(context, AutoHostUtils.DEFAULT_PACK_ID);
    }

    @Override
    public String getFilePath(PacketContext context, Identifier identifier) {
        return AutoHostUtils.getPathFromId(identifier);
    }
}
