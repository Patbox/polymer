package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import eu.pb4.polymer.resourcepack.api.metadata.PackMcMeta;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

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

    public static final SimpleEvent<Consumer<ResourcePackBuilder>> RESOURCE_PACK_CREATION_EVENT = INSTANCE.creationEvent;
    public static final SimpleEvent<Consumer<ResourcePackBuilder>> RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT = INSTANCE.afterInitialCreationEvent;
    public static final SimpleEvent<Runnable> RESOURCE_PACK_FINISHED_EVENT = INSTANCE.finishedEvent;
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
    public static boolean hasPack(@Nullable ServerPlayer player, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(player, uuid);
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
     * @param handler Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasPack(ServerCommonPacketListenerImpl handler, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(handler, uuid);
    }


    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param player Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasMainPack(@Nullable ServerPlayer player) {
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

    public static ResourcePackBuilder createBuilder(Path output) {
        return new DefaultRPBuilder(ResourcePackBuilder.OutputGenerator.zipGenerator(output), (s) -> {});
    }

    public static ResourcePackBuilder createBuilder(ResourcePackBuilder.OutputGenerator generator   ) {
        return new DefaultRPBuilder(generator, (s) -> {});
    }

    public static boolean buildMain() {
        return buildMain(PolymerResourcePackUtils.getMainPath());
    }

    public static boolean buildMain(Path output) {
        return buildMain(output, (s) -> {});
    }

    public static boolean buildMain(Path output, Consumer<String> status) {
        try {
            return INSTANCE.build(output, status);
        } catch (Exception e) {
            CommonImpl.LOGGER.error("Couldn't create resource pack!");
            e.printStackTrace();
            return false;
        }
    }

    static {
        INSTANCE.creationEvent.register((builder) -> {
            var config = PolymerResourcePackImpl.loadConfig();

            if (!config.preventedPaths.isEmpty()) {
                builder.addResourceConverter((path, data) -> {
                    for (var test : config.preventedPaths) {
                        if (path.startsWith(test)) {
                            return null;
                        }
                    }
                    return data;
                });
            }

            Path path = CommonImpl.getGameDir().resolve("polymer/source_assets");
            if (Files.isDirectory(path)) {
                builder.copyFromPath(path);
                try {
                    var metafile = path.resolve("pack.mcmeta");
                    if (Files.exists(metafile)) {
                        var meta = PackMcMeta.fromString(Files.readString(metafile));
                        builder.getPackMcMetaBuilder().metadata(meta.pack());
                    } else if (PolymerResourcePackImpl.IGNORE_PACK_VERSION) {
                        var og = builder.getPackMcMetaBuilder().metadata();
                        builder.getPackMcMetaBuilder().metadata(new PackMetadataSection(og.description(), new InclusiveRange<>(
                                SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES),
                                new PackFormat(Integer.MAX_VALUE, Integer.MAX_VALUE)
                        )));
                    }
                } catch (Throwable ignored) {}
            }

            try {
                for (var field : config.includeModAssets) {
                    builder.copyAssets(field);
                }
                var gamePath = FabricLoader.getInstance().getGameDir();

                Consumer<Path> zipReader = (zipPath) -> {
                    if (Files.exists(zipPath)) {
                        try (var fs = FileSystems.newFileSystem(zipPath)) {
                            for (var root : fs.getRootDirectories()) {
                                builder.copyResourcePackFromPath(root, zipPath.getFileName().toString());
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                };

                for (var field : config.includeZips) {
                    var parts = field.split("/");
                    if (parts.length == 0) continue;

                    if (parts[parts.length - 1].contains("*")) {
                        var folderPath = gamePath.resolve(String.join("/", Arrays.copyOfRange(parts, 0, parts.length - 1)));
                        if (!Files.isDirectory(folderPath)) {
                            continue;
                        }
                        try (var stream = Files.newDirectoryStream(folderPath, parts[parts.length - 1])){
                            stream.forEach(zipReader);
                        }
                    } else {
                        zipReader.accept(gamePath.resolve(field));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (CompatStatus.POLYMC) {
                try {
                    Files.createDirectories(path);
                    PolyMcHelpers.importPolyMcResources(builder);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        INSTANCE.afterInitialCreationEvent.register((builder) -> {
            Path path = CommonImpl.getGameDir().resolve("polymer/override_assets");
            if (Files.isDirectory(path)) {
                builder.copyFromPath(path);
                try {
                    var metafile = path.resolve("pack.mcmeta");
                    if (Files.exists(metafile)) {
                        var meta = PackMcMeta.fromString(Files.readString(metafile));
                        builder.getPackMcMetaBuilder().metadata(meta.pack());
                    }
                } catch (Throwable ignored) {}
            }
        });
    }

    public static ResourcePackCreator getInstance() {
        return INSTANCE;
    }
}
