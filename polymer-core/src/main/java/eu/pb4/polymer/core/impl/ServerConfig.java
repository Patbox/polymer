package eu.pb4.polymer.core.impl;

public class ServerConfig {
    public String _c5 = "Logs warnings while creating template/filter entities";
    public boolean enableTemplateEntityWarnings = true;
    public String _c7 = "Displays vanilla/modded creatives tabs in /polymer creative";
    public boolean displayNonPolymerCreativeTabs = true;
    public String _c9 = "Makes server send additional block updates around clicked area";
    public boolean sendBlocksAroundClicked = true;
    public String _c10a = "If you have too many BlockStates, this packets likes to break. This toggle allows you to disable";
    public String _c10b = "and replace it with multiple regular block update ones.";
    public String _c10c = "See this issue if you are curious: https://github.com/ConsistencyPlus/ConsistencyPlus/issues/108";
    public boolean disableChunkDeltaUpdatePacket = false;
    public String _c11 = "Makes polymer report time it's handshake took";
    public boolean logHandshakeTime = false;
    public String _c12 = "Enables logging of BlockState ids rebuilds";
    public boolean logBlockStateRebuilds = true;
    public String _c13 = "Enables logging of more exceptions. Useful when debugging";
    public boolean logAllExceptions = false;
    public String _c1 = "Enables syncing of non-polymer entries as polymer ones, when PolyMc is present";
    public boolean polyMcSyncModdedEntries = true;
}
