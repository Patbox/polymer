package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import eu.pb4.polymer.common.impl.CompatStatus;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackImpl;
import eu.pb4.polymer.resourcepack.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * Global utilities allowing creation of single, polymer mod compatible resource pack
 */
public final class PolymerResourcePackUtils {
    public static final Path DEFAULT_PATH = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack.zip").toAbsolutePath().normalize();

    private PolymerResourcePackUtils() {
    }

    private static final ResourcePackCreator INSTANCE = new ResourcePackCreator(PolymerResourcePackImpl.USE_OFFSET ? PolymerResourcePackImpl.OFFSET_VALUES : 1);

    public static final SimpleEvent<Consumer<PolymerRPBuilder>> RESOURCE_PACK_CREATION_EVENT = INSTANCE.creationEvent;
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
     * Allows to check if player has server side resoucepack installed
     * However it's impossible to check if it's polymer one or not
     *
     * @param player Player to check
     * @return True if player has a server resourcepack
     */
    public static boolean hasPack(@Nullable ServerPlayerEntity player) {
        return player != null && ((CommonResourcePackInfoHolder) player).polymerCommon$hasResourcePack();
    }

    /**
     * Sets resource pack status of player
     *
     * @param player Player to change status
     * @param status true if player has resource pack, otherwise false
     */
    public static void setPlayerStatus(ServerPlayerEntity player, boolean status) {
        ((CommonResourcePackInfoHolder) player).polymerCommon$setResourcePack(status);
        if (player.networkHandler != null) {
            ((CommonResourcePackInfoHolder) player.networkHandler).polymerCommon$setResourcePack(status);
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
        ((CommonResourcePackInfoHolder) player.networkHandler).polymerCommon$setIgnoreNextResourcePack();
    }

    public static PolymerRPBuilder createBuilder(Path output) {
        return new DefaultRPBuilder(output);
    }

    public static boolean build() {
        return build(PolymerResourcePackUtils.DEFAULT_PATH);
    }

    public static boolean build(Path output) {
        return build(output, (s) -> {});
    }

    public static boolean build(Path output, Consumer<String> status) {
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
