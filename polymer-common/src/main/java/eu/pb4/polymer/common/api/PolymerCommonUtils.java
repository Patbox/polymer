package eu.pb4.polymer.common.api;

import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PolymerCommonUtils {
    private PolymerCommonUtils(){}

    private final static String SAFE_CLIENT_SHA1 = "c0898ec7c6a5a2eaa317770203a1554260699994";
    private final static String SAFE_CLIENT_URL = "https://launcher.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";

    @Nullable
    public static Path getClientJar() {
        try {
            Path clientJarPath;
            if (!CommonImpl.IS_CLIENT) {
                clientJarPath = CommonImpl.getGameDir().resolve("polymer/cached_client_jars/" + SAFE_CLIENT_SHA1 + ".jar");
            } else {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                clientJarPath = Path.of(clientFile);
            }

            if (!Files.exists(clientJarPath)) {
                Files.createDirectories(clientJarPath.getParent());
                CommonImpl.LOGGER.info("Downloading vanilla client jar...");
                URL url = new URL(SAFE_CLIENT_URL);
                URLConnection connection = url.openConnection();
                InputStream is = connection.getInputStream();
                Files.copy(is, clientJarPath);
            }

            return clientJarPath;
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Couldn't retrieve client jar!", e);
            return null;
        }
    }
}
