package eu.pb4.polymer.autohost.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import eu.pb4.polymer.common.impl.CommonImpl;

public class AutoHostConfig {
    public String _c1 = "Enables Polymer's ResourcePack Auto Hosting";
    public boolean enableHttpServer = CommonImpl.DEV_ENV;
    public String _c2 = "Port used internally to run http server";
    public int port = 25567;
    public String _c3 = "Public address used for sending requests";
    public String externalAddress = "http://localhost:25567/";
    public String _c4 = "Marks resource pack as required";
    public boolean require = false;
    public String _c5 = "Message sent to clients before pack is loaded";
    public JsonElement message = new JsonPrimitive("This server uses resource pack to enhance gameplay with custom textures and models. It might be unplayable without them.");
    public String _c6 = "Disconnect message in case of failure";
    public JsonElement disconnectMessage = new JsonPrimitive("Couldn't apply server resourcepack!");
}
