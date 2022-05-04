package eu.pb4.polymer.impl;

public class ServerConfig {
    public String _c1 = "Keep this one at 0, unless you credit this library in another way";
    public int coreCommandOperatorLevel = 0;
    public String _c2 = "Marks resource pack as forced, only effects clients and mods using api to check it";
    public boolean markResourcePackAsForcedByDefault = false;
    public String _c3 = "Force-enables offset of CustomModelData";
    public boolean forcePackOffset = false;
    public String _c4 = "Toggles Polymer's networking globally. It's recommended to leave it to true for better experience";
    public boolean enableNetworkSync = true;
    public String _c5 = "Logs warnings while creating template/filter entities";
    public boolean enableTemplateEntityWarnings = true;
    public String _c6 = "Enables Polymer dev tools on non-development server";
    public boolean enableDevUtils = false;
    public String _c7 = "Displays vanilla/modded creatives tabs in /polymer creative";
    public boolean displayNonPolymerCreativeTabs = true;
    public String _c8 = "Toggles pre-play stage Polymer handshake. It's recommended to leave it to true";
    public boolean handleHandshakeEarly = true;
    public String _c9 = "Makes server send additional block updates around clicked area";
    public boolean sendBlocksAroundClicked = true;
    public String _c10a = "If you have too many BlockStates, this packets likes to break. This toggle allows you to disable";
    public String _c10b = "and replace it with multiple regular block update ones.";
    public String _c10c = "See this issue if you are curious: https://github.com/ConsistencyPlus/ConsistencyPlus/issues/108";
    public boolean disableChunkDeltaUpdatePacket = false;
}
