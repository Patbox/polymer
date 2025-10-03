package eu.pb4.polymer.common.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.common.impl.compat.FloodGateUtils;
import eu.pb4.polymer.common.impl.compat.ViaVersionUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;
import xyz.nucleoid.packettweaker.impl.MutableContext;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

public final class PolymerCommonUtils {
    public static final SimpleEvent<ResourcePackChangeCallback> ON_RESOURCE_PACK_STATUS_CHANGE = new SimpleEvent<>();
    private static final ThreadLocal<LogicOverride> FORCE_NETWORKING = ThreadLocal.withInitial(() -> LogicOverride.DEFAULT);
    private final static String SAFE_CLIENT_SHA1 = "ce92fd8d1b2460c41ceda07ae7b3fe863a80d045";
    private final static String SAFE_CLIENT_URL = "https://piston-data.mojang.com/v1/objects/" + SAFE_CLIENT_SHA1 + "/client.jar";
    private static Path cachedClientPath;
    private static Path cachedClientJarRoot;
    private PolymerCommonUtils() {
    }

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

    public static void executeWithNetworkingLogic(Runnable runnable) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);
        runnable.run();
        FORCE_NETWORKING.set(val);
    }

    public static void executeWithNetworkingLogic(PacketListener listener, Runnable runnable) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);
        PacketContext.runWithContext(listener, runnable);
        FORCE_NETWORKING.set(val);
    }

    public static void executeWithoutNetworkingLogic(Runnable runnable) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.FALSE);
        try {
            PacketContext.runWithContext(null, runnable);
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }

    public static <T> T executeWithNetworkingLogic(Supplier<T> supplier) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);
        try {
            return supplier.get();
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }

    public static <T> T executeWithNetworkingLogic(PacketListener listener, Supplier<T> supplier) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);
        try {
            return PacketContext.supplyWithContext(listener, supplier);
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }

    public static <T> T executeWithoutNetworkingLogic(Supplier<T> supplier) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.FALSE);
        try {
            return PacketContext.supplyWithContext(null, supplier);
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }

    public static ScopedOverride executeWithNetworkingLogic() {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);

        return () -> FORCE_NETWORKING.set(val);
    }

    public static ScopedOverride executeWithNetworkingLogic(PacketListener listener) {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.TRUE);
        var connection = MutableContext.get().getClientConnection();
        var packet = MutableContext.get().getEncodedPacket();
        MutableContext.get().set(listener, null);

        return () -> {
            MutableContext.get().set(connection, packet);
            FORCE_NETWORKING.set(val);
        };
    }

    public static ScopedOverride executeWithoutNetworkingLogic() {
        var val = FORCE_NETWORKING.get();
        FORCE_NETWORKING.set(LogicOverride.FALSE);
        var connection = MutableContext.get().getClientConnection();
        var packet = MutableContext.get().getEncodedPacket();
        MutableContext.get().clear();

        return () -> {
            MutableContext.get().set(connection, packet);
            FORCE_NETWORKING.set(val);
        };
    }


    public static World getFakeWorld() {
        return FakeWorld.INSTANCE;
    }

    public static boolean isNetworkingThread() {
        return FORCE_NETWORKING.get().value(Thread.currentThread().getName().startsWith("Netty"));
    }

    public static boolean isServerNetworkingThread() {
        return FORCE_NETWORKING.get().value(
                Thread.currentThread().getName().startsWith("Netty") && Thread.currentThread().getName().contains("Server")
        );
    }

    public static boolean isClientNetworkingThread() {
        return CommonImpl.IS_CLIENT && FORCE_NETWORKING.get().value(
                Thread.currentThread().getName().startsWith("Netty") && Thread.currentThread().getName().contains("Client")
        );
    }

    public static boolean isBedrockPlayer(ServerPlayerEntity player) {
        if (CompatStatus.FLOODGATE) {
            return FloodGateUtils.isPlayerBroken(player);
        }
        return false;
    }

    public static boolean isBedrockPlayer(GameProfile profile) {
        if (CompatStatus.FLOODGATE) {
            return FloodGateUtils.isPlayerBroken(profile.id());
        }
        return false;
    }

    public static int getPlayerGameProtocol(ServerPlayerEntity player) {
        return getPlayerGameProtocol(player.getGameProfile());
    }

    public static int getPlayerGameProtocol(GameProfile profile) {
        if (CompatStatus.VIAVERSION) {
            return ViaVersionUtils.getProtocol(profile.id());
        }
        return SharedConstants.getGameVersion().protocolVersion();
    }

    public static boolean hasResourcePack(@Nullable ServerPlayerEntity player, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || (player != null && player.networkHandler != null && ((CommonClientConnectionExt) ((CommonNetworkHandlerExt) player.networkHandler)
                .polymerCommon$getConnection()).polymerCommon$hasResourcePack(uuid))
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean hasResourcePack(ServerCommonNetworkHandler handler, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || (((CommonClientConnectionExt) ((CommonNetworkHandlerExt) handler).polymerCommon$getConnection()).polymerCommon$hasResourcePack(uuid))
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean hasResourcePack(ClientConnection connection, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || ((CommonClientConnectionExt) connection).polymerCommon$hasResourcePack(uuid)
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean hasResourcePack(PacketContext context, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || context.getClientConnection() != null && hasResourcePack(context.getClientConnection(), uuid);
    }

    public static boolean isServerBound() {
        if (CommonImpl.IS_CLIENT) {
            return ClientUtils.isSingleplayer();
        }

        return true;
    }

    public static void setHasResourcePack(ServerPlayerEntity player, UUID uuid, boolean status) {
        ((CommonClientConnectionExt) ((CommonNetworkHandlerExt) player.networkHandler).polymerCommon$getConnection()).polymerCommon$setResourcePack(uuid, status);
    }

    public static void setHasResourcePack(ClientConnection player, UUID uuid, boolean status) {
        ((CommonClientConnectionExt) player).polymerCommon$setResourcePack(uuid, status);
    }

    /**
     * Creates instance of object by using unsafe, bypassing initializers.
     * All of its fields will be set to null or similar.
     * <p>
     * Useful for bad packet implementations™™
     *
     * @param clazz class to instantiate
     * @param <T>   Anything you want
     * @return New instance
     */
    public static <T> T createUnsafe(Class<T> clazz) {
        return CommonImplUtils.createUnsafe(clazz);
    }

    public static boolean isServerNetworkingThreadWithContext() {
        return isServerNetworkingThread() && PacketContext.get().getClientConnection() != null;
    }

    public interface ResourcePackChangeCallback {
        void onResourcePackChange(ServerCommonNetworkHandler handler, UUID uuid, boolean oldStatus, boolean newStatus);
    }
}
