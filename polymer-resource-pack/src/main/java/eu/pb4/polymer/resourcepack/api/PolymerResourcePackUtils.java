package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.*;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Global utilities allowing creation of single, polymer mod compatible resource pack
 */
public final class PolymerResourcePackUtils {
    private static final Path DEFAULT_PATH = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip").toAbsolutePath().normalize();

    private PolymerResourcePackUtils() {
    }

    private static final ResourcePackCreator INSTANCE = new ResourcePackCreator(PolymerResourcePackImpl.USE_OFFSET ? PolymerResourcePackImpl.OFFSET_VALUES : 1);

    public static final SimpleEvent<Consumer<ResourcePackBuilder>> RESOURCE_PACK_CREATION_EVENT = INSTANCE.creationEvent;
    public static final SimpleEvent<Consumer<ResourcePackBuilder>> RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT = INSTANCE.afterInitialCreationEvent;
    public static final SimpleEvent<Runnable> RESOURCE_PACK_FINISHED_EVENT = INSTANCE.finishedEvent;
    private static boolean REQUIRED = PolymerResourcePackImpl.FORCE_REQUIRE;
    private static boolean DEFAULT_CHECK = true;

    /**
     * This method can be used to register custom model data for items
     *
     * @param vanillaItem Vanilla/Client side item
     * @param modelPath   Path to model in resource pack
     * @return PolymerModelData with data about this model
     */
    public static PolymerModelData requestModel(Item vanillaItem, Identifier modelPath) {
        return INSTANCE.requestModel(vanillaItem, modelPath);
    }

    /**
     * This method can be used to register custom model data for items
     *
     * @param modelPath Path to model in resource pack
     * @return PolymerArmorModel with data about this model
     */
    public static PolymerArmorModel requestArmor(Identifier modelPath) {
        return INSTANCE.requestArmor(modelPath);
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public static boolean addModAssets(String modId) {
        return INSTANCE.addAssetSource(modId);
    }

    /**
     * Adds mod with provided mod id as a source of assets
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
    public static boolean hasPack(@Nullable ServerPlayerEntity player, UUID uuid) {
        return PolymerCommonUtils.hasResourcePack(player, uuid);
    }

    /**
     * Allows to check if player has selected server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param player Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasMainPack(@Nullable ServerPlayerEntity player) {
        return hasPack(player, getMainUuid());
    }

    public static Path getMainPath() {
        return DEFAULT_PATH;
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
    public static void setPlayerStatus(ServerPlayerEntity player, UUID uuid, boolean status) {
        //((CommonClientConnectionExt) player).polymerCommon$setResourcePack(status);
        if (player.networkHandler != null) {
            ((CommonClientConnectionExt) player.networkHandler).polymerCommon$setResourcePack(uuid, status);
        }
    }

    /**
     * Returns true if color is taken
     */
    public static boolean isColorTaken(int color) {
        return INSTANCE.isColorTaken(color);
    }

    /**
     * Gets an unmodifiable list of models for an item.
     * This can be useful if you need to extract this list and parse it yourself.
     *
     * @param item Item you want list for
     * @return An unmodifiable list of models
     */
    public static List<PolymerModelData> getModelsFor(Item item) {
        return INSTANCE.getModelsFor(item);
    }

    public static void disableDefaultCheck() {
        DEFAULT_CHECK = false;
        CommonImplUtils.disableResourcePackCheck = true;
    }

    public static boolean shouldCheckByDefault() {
        return DEFAULT_CHECK;
    }

    public static void ignoreNextDefaultCheck(ServerPlayerEntity player) {
        ((CommonNetworkHandlerExt) player.networkHandler).polymerCommon$setIgnoreNextResourcePack();
    }

    public static ResourcePackBuilder createBuilder(Path output) {
        return new DefaultRPBuilder(output);
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
            Path path = CommonImpl.getGameDir().resolve("polymer/source_assets");
            if (Files.isDirectory(path)) {
                builder.copyFromPath(path);
            }

            try {
                for (var field : PolymerResourcePackImpl.INCLUDE_MOD_IDS) {
                    builder.copyAssets(field);
                }
                var gamePath = FabricLoader.getInstance().getGameDir();

                for (var field : PolymerResourcePackImpl.INCLUDE_ZIPS) {
                    var zipPath = gamePath.resolve(field);

                    if (Files.exists(zipPath)) {
                        try (var fs = FileSystems.newFileSystem(zipPath)) {
                            builder.copyFromPath(fs.getPath("assets"), "assets/");

                            for (var root : fs.getRootDirectories()) {
                                try (var str = Files.list(root)) {
                                    str.forEach(file -> {
                                        try {
                                            var name = file.getFileName().toString();
                                            if (name.toLowerCase(Locale.ROOT).contains("license")
                                                    || name.toLowerCase(Locale.ROOT).contains("licence")) {
                                                builder.addData("licenses/"
                                                        + field.replace("/", "_").replace("\\", "_") + "/" + name, Files.readAllBytes(file));
                                            }
                                        } catch (Throwable ignored) {}
                                    });
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
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
            }
        });
    }

    public static ResourcePackCreator getInstance() {
        return INSTANCE;
    }
}
