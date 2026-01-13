package eu.pb4.polymer.autohost.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.common.impl.CommonImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class AutoHostConfig {
    public String _c1 = "Enables Polymer's ResourcePack Auto Hosting";
    @SerializedName("enabled")
    public boolean enabled = CommonImpl.DEV_ENV;
    public String _c2 = "Marks resource pack as required";
    @SerializedName("required")
    public boolean require = false;
    public String _c7 = "Mods may override the above setting and make the resource pack required, set this to false to disable that.";
    @SerializedName("mod_override")
    public boolean modOverride = true;
    public String _c3 = "Type of resource pack provider. Default: 'polymer:automatic'";
    public String type = "polymer:automatic";
    public String _c4 = "Configuration of type, see provider's source for more details";
    @SerializedName("settings")
    public JsonElement providerSettings = new JsonObject();
    public String _c5 = "Message sent to clients before pack is loaded";
    public JsonElement message = new JsonPrimitive("This server uses resource pack to enhance gameplay with custom textures and models. It might be unplayable without them.");
    public String _c6 = "Disconnect message in case of failure";
    @SerializedName("disconnect_message")
    public JsonElement disconnectMessage = new JsonPrimitive("{\"translate\":\"multiplayer.requiredTexturePrompt.disconnect\"}");
    public String _c20 = "Show more information when disconnecting the player";
    @SerializedName("informative_disconnect")
    public boolean informativeDisconnect = true;
    public String _c8 = "Allows to define more external resource packs. It's an object with 'id' for uuid, 'url' for the pack url and 'hash' for the SHA1 hash.";
    @SerializedName("external_resource_packs")
    public List<ExternalResourcePack> externalResourcePacks = new ArrayList<>();
    public String _c9 = "Moves resource pack generation earlier when running on server. Might break some mods.";
    @SerializedName("setup_early")
    public boolean loadEarly = CommonImpl.DEV_ENV;
    public String _c10 = "Enables dialog infobox when resource pack isn't ready.";
    @SerializedName("resource_pack_status_dialog")
    public boolean dialog = true;
    public String _c11 = "Enables dialog infobox when resource pack isn't ready.";
    @SerializedName("dialog_title")
    public JsonElement dialogTitle = new JsonPrimitive("The server's resource pack is still generating!");
    public String _c12 = "Default body text before status is ready (or when it's disabled).";
    @SerializedName("dialog_default_body")
    public JsonElement dialogDefaultBody = new JsonPrimitive("Waiting...");
    public String _c17 = "Text below name with some extra information about resource pack generation";
    @SerializedName("dialog_body_header")
    public JsonElement dialogHeader = new JsonPrimitive("This server requires a resource pack, which hasn't finished generating yet...\nIt might take a while for it to finish!");
    public String _c13 = "Enables displaying internal resource pack generation status.";
    @SerializedName("dialog_show_status")
    public boolean dialogShowStatus = true;
    public String _c18 = "Enables displaying 'dots' indicating that work is being done!";
    @SerializedName("dialog_show_dots")
    public boolean dialogShowDots = true;
    public String _c14 = "Clears all client-side resourcepacks before sending Autohost handled ones.";
    @SerializedName("clear_all_client_resource_packs")
    public boolean clearResourcePacks = false;
    public String _c15 = "Adds hash to name of resource pack file served with web server";
    @SerializedName("include_hash_in_name")
    public boolean includeHashInName = true;
    public String _c16 = "Value of Cache-Control max age header";
    @SerializedName("cache_control_max_age")
    public int cacheControlAge = 31536000;

    public String _c19 = "Delays server from showing as online on player list until resource pack is generated.";
    @SerializedName("delay_player_list_motd_until_generated")
    public boolean delayPlayerListMotd = false;

    public static class ExternalResourcePack {
        public UUID id;
        public String url;
        public String hash;
    }
}