package eu.pb4.polymer.common.api;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.FakeWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PolymerCommonUtils {
    private PolymerCommonUtils(){}

    public static final SimpleEvent<ResourcePackChangeCallback> ON_RESOURCE_PACK_STATUS_CHANGE = new SimpleEvent<>();

    private final static String SAFE_CLIENT_SHA1 = "0f37b64668c8b2e54d12bd13138f8ca874f92270";
    private final static String SAFE_CLIENT_URL = "https://piston-data.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";
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

    public static World getFakeWorld() {
        return FakeWorld.INSTANCE;
    }

    public interface ResourcePackChangeCallback {
        void onResourcePackChange(ServerPlayNetworkHandler handler, boolean oldStatus, boolean newStatus);
    }
}
