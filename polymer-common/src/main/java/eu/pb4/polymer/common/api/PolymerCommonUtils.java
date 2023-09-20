package eu.pb4.polymer.common.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.common.impl.compat.FloodGateUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PolymerCommonUtils {
    private PolymerCommonUtils(){}

    public static final SimpleEvent<ResourcePackChangeCallback> ON_RESOURCE_PACK_STATUS_CHANGE = new SimpleEvent<>();
    private static Path cachedClientPath;
    private final static String SAFE_CLIENT_SHA1 = "e575a48efda46cf88111ba05b624ef90c520eef1";
    private final static String SAFE_CLIENT_URL = "https://piston-data.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";
    private static Path cachedClientJarRoot;

    @Nullable
    public static Path getClientJarRoot() {
        if (cachedClientJarRoot != null) {
            return cachedClientJarRoot;
        }

        if (CommonImpl.IS_CLIENT) {
            var container = FabricLoader.getInstance().getModContainer("minecraft").get();
            for (var x : container.getRootPaths()) {
                if (Files.exists(x.resolve("assets"))) {
                    cachedClientJarRoot = x;
                    return x;
                }
            }
        }
        var source = getClientJar();

        if (source == null) {
            return null;
        }

        try {
            var fs = FileSystems.newFileSystem(source);
            for (var x : fs.getRootDirectories()) {
                if (Files.exists(x.resolve("assets"))) {
                    cachedClientJarRoot = x;
                    return x;
                }
            }
            fs.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static Path getClientJar() {
        if (cachedClientPath != null) {
            return cachedClientPath;
        }

        try {
            if (CommonImpl.IS_CLIENT) {
                var clientFile = MinecraftServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                var clientJarPath = Path.of(clientFile);
                if (Files.exists(clientJarPath)) {
                    try (var fs = FileSystems.newFileSystem(clientJarPath)) {
                        if (Files.exists(fs.getPath("/"))) {
                            cachedClientPath = clientJarPath;
                            return cachedClientPath;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
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
            cachedClientPath = clientJarPath;
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
        var oldTarget = PacketContext.get().getPlayer();
        var oldPacket = PacketContext.get().getEncodedPacket();

        CommonImplUtils.setPlayer(player);
        PacketContext.setContext(((CommonNetworkHandlerExt)player.networkHandler).polymerCommon$getConnection(), null);

        runnable.run();

        CommonImplUtils.setPlayer(oldPlayer);
        PacketContext.setContext(oldTarget != null ? ((CommonNetworkHandlerExt)player.networkHandler).polymerCommon$getConnection() : null, oldPacket);
    }


    public static World getFakeWorld() {
        return FakeWorld.INSTANCE;
    }

    /**
     * Returns player if it's known to polymer (otherwise null!)
     */
    @Nullable
    public static ServerPlayerEntity getPlayerContext() {
        ServerPlayerEntity player = getPlayerContextNoClient();
        if (player == null && CommonImpl.IS_CLIENT) {
            player = ClientUtils.getPlayer();
        }

        return player;
    }

    @Nullable
    public static ServerPlayerEntity getPlayerContextNoClient() {
        ServerPlayerEntity player = PacketContext.get().getPlayer();

        if (player == null) {
            player = CommonImplUtils.getPlayer();
        }

        return player;
    }

    public static boolean isNetworkingThread() {
       return Thread.currentThread().getName().startsWith("Netty");
    }

    public static boolean isServerNetworkingThread() {
        return isNetworkingThread() && Thread.currentThread().getName().contains("Server");
    }

    public static boolean isBedrockPlayer(ServerPlayerEntity player) {
        if (CompatStatus.FLOODGATE) {
            return FloodGateUtils.isPlayerBroken(player);
        }
        return false;
    }

    public static boolean isBedrockPlayer(GameProfile profile) {
        if (CompatStatus.FLOODGATE) {
            return FloodGateUtils.isPlayerBroken(profile);
        }
        return false;
    }

    public static boolean hasResourcePack(@Nullable ServerPlayerEntity player) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || (player != null && player.networkHandler != null && ((CommonClientConnectionExt) ((CommonNetworkHandlerExt) player.networkHandler)
                .polymerCommon$getConnection()).polymerCommon$hasResourcePack())
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean hasResourcePack(ClientConnection connection) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || ((CommonClientConnectionExt) connection).polymerCommon$hasResourcePack()
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean isServerBound() {
        if (CommonImpl.IS_CLIENT) {
            return ClientUtils.isSingleplayer();
        }

        return true;
    }

    public static void setHasResourcePack(ServerPlayerEntity player, boolean status) {
        ((CommonClientConnectionExt) ((CommonNetworkHandlerExt) player.networkHandler).polymerCommon$getConnection()).polymerCommon$setResourcePack(status);
    }

    public interface ResourcePackChangeCallback {
        void onResourcePackChange(ServerPlayNetworkHandler handler, boolean oldStatus, boolean newStatus);
    }
}
