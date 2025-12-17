package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.autohost.impl.ConnectionExt;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;

public class NettyProvider extends AbstractProvider {
    private Config config = new Config();

    @Override
    public JsonElement saveSettings() {
        return CommonImpl.GSON.toJsonTree(this.config);
    }

    @Override
    public void loadSettings(JsonElement settings) {
        try {
            this.config = CommonImpl.GSON.fromJson(settings, Config.class);
        } catch (Throwable e) {
            this.config = new Config();
        }

    }

    @Override
    public void serverStopped(MinecraftServer server) {}

    @Override
    protected String getAddress(Connection connection, String file) {
        if (this.config.forcedAddress.isEmpty()) {
            return "http://" + ((ConnectionExt) connection).polymerAutoHost$getFullAddress() + "/eu.pb4.polymer.autohost/" + file;
        } else {
            return this.config.forcedAddress + "/eu.pb4.polymer.autohost/" + file;
        }
    }

    public static class Config {
        @SerializedName(value = "forced_address", alternate = {"address", "external_address"})
        public String forcedAddress = "";
    }
}
