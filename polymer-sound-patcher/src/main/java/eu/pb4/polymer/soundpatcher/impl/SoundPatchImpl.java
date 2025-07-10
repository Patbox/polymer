package eu.pb4.polymer.soundpatcher.impl;

import com.google.gson.annotations.SerializedName;
import eu.pb4.polymer.common.impl.CommonImpl;

public class SoundPatchImpl {
    public static final boolean FORCE_DISABLE;
    public static boolean VANILLA_BLOCK_SOUNDS;
    public static final boolean MODDED_BLOCK_SOUNDS;


    static {
        var config = loadConfig();

        FORCE_DISABLE = config.forceDisable;
        VANILLA_BLOCK_SOUNDS = config.alwaysHandleVanillaBlockSounds;
        MODDED_BLOCK_SOUNDS = config.alwaysSendModdedBlockSounds;
    }

    public static Config loadConfig() {
        return CommonImpl.loadAndRegisterConfig("sound-patch", Config.class);
    }


    public static class Config {
        public String _c0 = "Forcefully disables all sound patches used by mods.";
        @SerializedName("force_disable")
        public boolean forceDisable = false;
        public String _c1 = "Marks all vanilla block sounds as server handled.";
        @SerializedName("always_handle_vanilla_block_sounds")
        public boolean alwaysHandleVanillaBlockSounds = false;
        public String _c2 = "Marks all modded block sounds to be sent to the client (useful with polymc).";
        @SerializedName("always_send_modded_block_sounds")
        public boolean alwaysSendModdedBlockSounds = false;
    }
}
