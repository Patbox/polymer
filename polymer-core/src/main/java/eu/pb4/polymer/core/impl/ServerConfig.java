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
    public String _c3 = "Forcefully enables strict block updates, making client desyncs less likely to happen";
    @SerializedName("force_strict_block_updates")
    public boolean forceStrictUpdates = false;
    public String _c4 = "Enables experimental passing of ItemStack context through nbt, allowing for better mod compat";
    @SerializedName("item_stack_nbt_hack")
    public boolean itemStackNbtHack = true;
}
