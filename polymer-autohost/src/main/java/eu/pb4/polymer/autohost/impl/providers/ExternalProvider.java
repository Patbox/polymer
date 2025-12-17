package eu.pb4.polymer.autohost.impl.providers;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.network.Connection;

public class ExternalProvider extends AbstractProvider {
    private Config config = new Config();

    @Override
    public JsonElement saveSettings() {
        return CommonImpl.GSON.toJsonTree(this.config);
    }

    @Override
    public void loadSettings(JsonElement settings) {
        this.config = CommonImpl.GSON.fromJson(settings, Config.class);
        if (this.config == null) {
            this.config = new Config();
        }
    }

    @Override
    protected String getAddress(Connection connection, String path) {
        return this.config.address + "/" + path;
    }


    public static class Config {
        @SerializedName("__gendesc")
        public String comment = "Full url (with file name) point to the generated resource pack file.";

        @SerializedName(value = "address", alternate = {"forced_address", "external_address"})
        public String address = "";
    }
}
