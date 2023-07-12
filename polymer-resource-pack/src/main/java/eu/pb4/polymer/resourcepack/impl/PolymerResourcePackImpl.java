package eu.pb4.polymer.resourcepack.impl;

import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CompatStatus;

public class PolymerResourcePackImpl {
    public static final boolean FORCE_REQUIRE;
    public static final boolean USE_OFFSET;
    public static final int OFFSET_VALUES;
    public static final boolean USE_ALT_ARMOR_HANDLER;


    static {
        var config = CommonImpl.loadConfig("resource-pack", Config.class);

        FORCE_REQUIRE = config.markResourcePackAsRequiredByDefault || CompatStatus.POLYMC;

        USE_OFFSET = config.forcePackOffset || CompatStatus.POLYMC;

        OFFSET_VALUES = config.offsetValue;

        USE_ALT_ARMOR_HANDLER = config.useAlternativeArmorHandler || CompatStatus.REQUIRE_ALT_ARMOR_HANDLER;
    }


    public static class Config {
        public String _c1 = "Marks resource pack as required, only effects clients and mods using api to check it";
        public boolean markResourcePackAsRequiredByDefault = false;

        public String _c2 = "Force-enables offset of CustomModelData";
        public boolean forcePackOffset = false;

        public String _c3 = "Value of CustomModelData offset when enabled";
        public int offsetValue = 100000;
        public String _c4 = "Enables usage of alternative armor rendering for increased mod compatibility";
        @SerializedName("use_alternative_armor_rendering")
        public boolean useAlternativeArmorHandler;
    }
}
