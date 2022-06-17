package eu.pb4.polymer.rsm.impl;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public class InitCheck {
    public static final Logger LOGGER = LoggerFactory.getLogger("Polymer RSM");

    public static void check() {
        var mod = FabricLoader.getInstance().getModContainer("polymer");
        try {
            if (mod.isPresent() && mod.get().getMetadata().getVersion().compareTo(Version.parse("0.2.1")) < 0) {
                LOGGER.error("Old Polymer version detected! Polymer Registry Sync Manipulator is incompatible with any version of Polymer older than 0.2.1!");
                LOGGER.error("Please update Polymer library! https://modrinth.com/mod/polymer / https://www.curseforge.com/minecraft/mc-mods/polymer");
            }
        } catch (Throwable e) {

        }
    }
}
