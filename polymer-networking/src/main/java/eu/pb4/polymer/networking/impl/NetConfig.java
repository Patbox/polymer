package eu.pb4.polymer.networking.impl;

import com.google.gson.annotations.SerializedName;

public class NetConfig {
    public String _c2 = "Forcefully disables networking between server and client. I suggest not disabling it for better mod compatibility and client extras.";
    @SerializedName("disable_player_networking")
    public boolean forceDisable = false;
}
