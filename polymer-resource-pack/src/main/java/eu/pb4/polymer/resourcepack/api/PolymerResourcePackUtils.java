package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Global utilities allowing creation of single, polymer mod compatible resource pack
 */
public final class PolymerResourcePackUtils {
    private PolymerResourcePackUtils() {
    }

    private static final ResourcePackCreator INSTANCE = new ResourcePackCreator();

    public static final Event<Runnable> RESOURCE_PACK_INITIALIZED_EVENT = INSTANCE.initializedEvent;
    public static final Event<Consumer<ResourcePackBuilder>> RESOURCE_PACK_CREATION_EVENT = INSTANCE.creationEvent;
    public static final Event<Consumer<ResourcePackBuilder>> RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT = INSTANCE.afterInitialCreationEvent;
    public static final Event<Consumer<Object>> RESOURCE_PACK_FINISHED_EVENT = INSTANCE.finishedEvent;

    private static boolean REQUIRED = PolymerResourcePackImpl.FORCE_REQUIRE;
    private static boolean DEFAULT_CHECK = true;

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public static boolean addModAssets(String modId) {
        return INSTANCE.addAssetSource(modId);
    }

    /**
     * Adds mod with provided mod id as a source of assets, without actually copying them to the resource pack
     *
     * @param modId Id of mods used as a source
     */
    public static boolean addModAssetsWithoutCopy(String modId) {
        return INSTANCE.addAssetSourceWithoutCopy(modId);
    }

    /**
     * Allows to check if there are any provided resources
     */
    public static boolean hasResources() {
        return !INSTANCE.isEmpty();
    }

    /**
     * Makes resource pack required
     */
    public static void markAsRequired() {
        REQUIRED = true;
    }

    /**
     * Returns if resource pack is required
     */
    public static boolean isRequired() {
        return REQUIRED;
    }

    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param player Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasPack(@Nullable PacketContextProvider provider, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(provider, uuid);
    }

    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param context Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasPack(PacketContext context, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(context, uuid);
    }

    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param player Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasMainPack(@Nullable PacketContextProvider player) {
        return hasPack(player, getMainUuid());
    }

    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param context Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasMainPack(PacketContext context) {
        return hasPack(context, getMainUuid());
    }
    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param handler Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasMainPack(ServerCommonPacketListenerImpl handler) {
        return hasPack(handler, getMainUuid());
    }

    public static Path getMainPath() {
        return PolymerResourcePackImpl.DEFAULT_PATH;
    }

    public static UUID getMainUuid() {
        return PolymerResourcePackImpl.MAIN_UUID;
    }

    /**
     * Sets resource pack status of player
     *
     * @param player Player to change status
     * @param status true if player has resource pack, otherwise false
     */
    public static void setPlayerStatus(ServerPlayer player, UUID uuid, boolean status) {
        //((CommonClientConnectionExt) player).polymerCommon$setResourcePack(status);
        if (player.connection != null) {
            ((CommonConnectionExt) player.connection).polymerCommon$setResourcePack(uuid, status);
        }
    }

    public static void disableDefaultCheck() {
        DEFAULT_CHECK = false;
        CommonImplUtils.disableResourcePackCheck = true;
    }

    public static boolean shouldCheckByDefault() {
        return DEFAULT_CHECK;
    }

    public static void ignoreNextDefaultCheck(ServerPlayer player) {
        ((CommonPacketListenerImplExt) player.connection).polymerCommon$setIgnoreNextResourcePack();
    }

    public static ResourcePackCreator getInstance() {
        return INSTANCE;
    }
}
