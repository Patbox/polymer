package eu.pb4.polymer.soundpatcher.impl;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.extras.api.format.sound.SoundsAsset;
import org.jspecify.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class VanillaSoundJson {
    private static final String HASH = "123370fbb15bf1e85cf5038b3e8ca719ad391dfb";
    private final static String SOUNDS_URL = "https://resources.download.minecraft.net/" + HASH.substring(0, 2) + "/" + HASH;

    private final static Path CHECKED_PATH = CommonImpl.getGameDir().resolve("polymer/cached_client_jars/" + HASH + "_sounds.json");

    @Nullable
    private static SoundsAsset soundAsset = null;

    public static SoundsAsset getSoundAsset() {
        if (soundAsset != null) {
            return soundAsset;
        }

        return soundAsset = createSoundAsset();
    }


    private static SoundsAsset createSoundAsset() {
        if (CommonImpl.IS_CLIENT) {
            
        }

        try {
            if (!Files.exists(CHECKED_PATH)) {
                Files.createDirectories(CHECKED_PATH.getParent());
                CommonImpl.LOGGER.info("Downloading vanilla sounds.json...");
                java.net.URL url = new URL(SOUNDS_URL);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                Files.copy(is, CHECKED_PATH);
            }
            var data = Files.readString(CHECKED_PATH);

            return SoundsAsset.fromJson(data);
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Couldn't retrieve vanilla sounds.json!", e);
            return SoundsAsset.builder().build();
        }
    }

}
