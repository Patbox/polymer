package eu.pb4.polymer.core.impl;

import com.google.gson.annotations.SerializedName;

public class ServerConfig {
    public String _c7 = "Displays vanilla/modded creatives tabs in /polymer creative";
    public boolean displayNonPolymerCreativeTabs = true;
    public String _c9 = "Makes server send additional block updates around clicked area";
    public boolean sendBlocksAroundClicked = true;
    public String _c11 = "Makes polymer report time it's handshake took";
    public boolean logHandshakeTime = false;
    public String _c12 = "Enables logging of BlockState ids rebuilds";
    public boolean logBlockStateRebuilds = true;
    public String _c1 = "Enables syncing of non-polymer entries as polymer ones, when PolyMc is present";
    public boolean polyMcSyncModdedEntries = true;
    public String _c2 = "Delay from last light updates to syncing it to clients, in ticks";
    public int lightUpdateTickDelay = 1;
    public String _c14 = "Amount of recipes that get split into seperate packets instead of being sent all at once. Should help avoiding packet size limit. -1 disables it";
    @SerializedName("split_recipe_book_packet_amount")
    public int splitRecipeBookPacket = -1;
    public String _c15 = "Changes stonecutter a bit to fix custom recipes not working with it.";
    @SerializedName("force_enable_stonecutter_fix")
    public boolean stonecutterFix = false;
    public String _c13 = "Replaces PolyMc's block and item interaction handling with Polymer ones";
    @SerializedName("override_polymc_mining_check")
    public boolean overridePolyMcMining = false;
}
