package eu.pb4.polymer.common.api;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.FakeWorld;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PolymerCommonUtils {
    private PolymerCommonUtils(){}

    public static final SimpleEvent<ResourcePackChangeCallback> ON_RESOURCE_PACK_STATUS_CHANGE = new SimpleEvent<>();

    private final static String SAFE_CLIENT_SHA1 = "958928a560c9167687bea0cefeb7375da1e552a8";
    private final static String SAFE_CLIENT_URL = "https://piston-data.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";
    @Nullable
    public static Path getClientJar() {
        try {
            if (CommonImpl.IS_CLIENT) {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                var clientJarPath = Path.of(clientFile);

                if (Files.exists(clientJarPath)) {
                    return clientJarPath;
                }
            }

            Path clientJarPath = CommonImpl.getGameDir().resolve("polymer/cached_client_jars/" + SAFE_CLIENT_SHA1 + ".jar");

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

    /**
     * Allows to execute code with selected player being returned for {@link PolymerCommonUtils#getPlayerContext()}
     * calls. Useful for custom packets using writeItemStack and similar methods.
     *
     * @param player
     * @param runnable
     */
    public static void executeWithPlayerContext(ServerPlayerEntity player, Runnable runnable) {
        var oldPlayer = CommonImplUtils.getPlayer();
        var oldTarget = PacketContext.get().getTarget();

        CommonImplUtils.setPlayer(player);
        PacketContext.setReadContext(player.networkHandler);

        runnable.run();

        CommonImplUtils.setPlayer(oldPlayer);
        PacketContext.setReadContext(oldTarget != null ? oldTarget.networkHandler : null);
    }


    public static World getFakeWorld() {
        return FakeWorld.INSTANCE;
    }

    /**
     * Returns player if it's known to polymer (otherwise null!)
     */
    @Nullable
    public static ServerPlayerEntity getPlayerContext() {
        ServerPlayerEntity player = PacketContext.get().getTarget();

        if (player == null) {
            player = CommonImplUtils.getPlayer();

            if (player == null && CommonImpl.IS_CLIENT) {
                player = ClientUtils.getPlayer();
            }
        }

        return player;
    }

    public interface ResourcePackChangeCallback {
        void onResourcePackChange(ServerPlayNetworkHandler handler, boolean oldStatus, boolean newStatus);
    }
}
