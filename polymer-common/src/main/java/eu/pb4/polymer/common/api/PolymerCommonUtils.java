package eu.pb4.polymer.common.api;

import com.mojang.authlib.GameProfile;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.common.impl.compat.FloodGateUtils;
import eu.pb4.polymer.common.impl.compat.ViaVersionUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

public final class PolymerCommonUtils {
    public static final Event<ResourcePackChangeCallback> ON_RESOURCE_PACK_STATUS_CHANGE = EventFactory.createArrayBacked(ResourcePackChangeCallback.class, arr -> (handler, uuid, oldStatus, newStatus) -> {
        for (var c : arr) {
            c.onResourcePackChange(handler, uuid, oldStatus, newStatus);
        }
    });
    private static final ThreadLocal<LogicOverride> FORCE_NETWORKING = ThreadLocal.withInitial(() -> LogicOverride.DEFAULT);
    private final static String SAFE_CLIENT_SHA1 = "93079d0d5608af9e10283be6be89b67a772d0c8f";
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

    @Deprecated(forRemoval = true)
    public static <T> T executeWithNetworkingLogic(Supplier<T> supplier) {
        var val = FORCE_NETWORKING.get();
        try {
            FORCE_NETWORKING.set(LogicOverride.TRUE);
            return supplier.get();
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }

    @Deprecated(forRemoval = true)
    public static <T> T executeWithoutNetworkingLogic(Supplier<T> supplier) {
        var val = FORCE_NETWORKING.get();
        try {
            FORCE_NETWORKING.set(LogicOverride.FALSE);
            return PacketContext.supplyWithContext(new PacketContextProvider() {
                @Override
                public PacketContext getPacketContext() {
                    return null;
                }
            }, supplier);
        } finally {
            FORCE_NETWORKING.set(val);
        }
    }


    public static Level getFakeWorld() {
        return FakeWorld.INSTANCE;
    }

    public static boolean isNetworkingThread() {
        return FORCE_NETWORKING.get().value(PacketContext.get() != null);
    }

    public static boolean isServerNetworkingThread() {
        return FORCE_NETWORKING.get().value(
                PacketContext.get() instanceof PacketContext context && context.orElseThrow(PacketContext.CONNECTION).getReceiving() == PacketFlow.SERVERBOUND
        );
    }

    public static boolean isClientNetworkingThread() {
        return CommonImpl.IS_CLIENT && FORCE_NETWORKING.get().value(
                PacketContext.get() instanceof PacketContext context && context.orElseThrow(PacketContext.CONNECTION).getReceiving() == PacketFlow.CLIENTBOUND
        );
    }

    public static boolean isBedrockPlayer(PacketContextProvider contextProvider) {
        return isBedrockPlayer(contextProvider.getPacketContext().get(PacketContext.GAME_PROFILE));
    }

    public static boolean isBedrockPlayer(GameProfile profile) {
        if (CompatStatus.FLOODGATE && profile != null) {
            return FloodGateUtils.isPlayerBroken(profile.id());
        }
        return false;
    }

    public static int getPlayerGameProtocol(PacketContextProvider contextProvider) {
        return getPlayerGameProtocol(contextProvider.getPacketContext().get(PacketContext.GAME_PROFILE));
    }

    public static int getPlayerGameProtocol(GameProfile profile) {
        if (CompatStatus.VIAVERSION && profile != null) {
            return ViaVersionUtils.getProtocol(profile.id());
        }
        return SharedConstants.getCurrentVersion().protocolVersion();
    }

    public static boolean hasResourcePack(@Nullable PacketContextProvider provider, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || provider != null && hasResourcePack(provider.getPacketContext(), uuid)
                || (CommonImpl.IS_CLIENT && ClientUtils.isResourcePackLoaded());
    }

    public static boolean hasResourcePack(@Nullable PacketContext context, UUID uuid) {
        return CommonImpl.FORCE_RESOURCEPACK_ENABLED_STATE
                || context != null && hasResourcePack(context.orElseThrow(PacketContext.CONNECTION), uuid);
    }

    public static boolean isServerBound() {
        if (CommonImpl.IS_CLIENT) {
            return ClientUtils.isSingleplayer();
        }

        return true;
    }

    public static void setHasResourcePack(PacketContextProvider provider, UUID uuid, boolean status) {
        setHasResourcePack(provider.getPacketContext(), uuid, status);
    }

    public static void setHasResourcePack(PacketContext context, UUID uuid, boolean status) {
        ((CommonConnectionExt) context.orElseThrow(PacketContext.CONNECTION)).polymerCommon$setResourcePack(uuid, status);
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

    public static @Nullable ServerPlayer getPlayer(PacketContext context) {
        return context.orElseThrow(PacketContext.CONNECTION).getPacketListener() instanceof ServerPlayerConnection connection ? connection.getPlayer() : null;
    }

    public interface ResourcePackChangeCallback {
        void onResourcePackChange(ServerCommonPacketListenerImpl handler, UUID uuid, boolean oldStatus, boolean newStatus);
    }
}
