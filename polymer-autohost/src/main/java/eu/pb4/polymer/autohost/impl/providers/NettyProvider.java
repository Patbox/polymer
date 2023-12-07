package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.pb4.polymer.autohost.impl.ClientConnectionExt;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;

public class NettyProvider extends AbstractProvider {
    public static final NettyProvider INSTANCE = new NettyProvider();
    @Override
    public JsonElement saveSettings() {
        return new JsonObject();
    }

    @Override
    public void loadSettings(JsonElement settings) {

    }

    @Override
    public void serverStopped(MinecraftServer server) {}

    @Override
    protected String getAddress(ClientConnection connection) {
        return "http://" + ((ClientConnectionExt) connection).polymerAutoHost$getFullAddress() + "/eu.pb4.polymer.autohost/" + this.hash + ".zip";
    }
}
