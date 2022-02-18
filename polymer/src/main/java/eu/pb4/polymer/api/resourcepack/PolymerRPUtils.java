package eu.pb4.polymer.api.resourcepack;

import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.compat.CompatStatus;
import eu.pb4.polymer.impl.compat.polymc.PolyMcHelpers;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
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
public final class PolymerRPUtils {
    private PolymerRPUtils() {
    }

    private static final ResourcePackCreator INSTANCE = new ResourcePackCreator(PolymerImpl.FORCE_CUSTOM_MODEL_DATA_OFFSET ? 100000 : 1);

    public static final SimpleEvent<Consumer<PolymerRPBuilder>> RESOURCE_PACK_CREATION_EVENT = INSTANCE.creationEvent;
    private static boolean REQUIRED = PolymerImpl.FORCE_RESOURCE_PACK_SERVER;
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
    public static boolean addAssetSource(String modId) {
        return INSTANCE.addAssetSource(modId);
    }

    /**
     * Allows to check if there are any provided resources
     */
    public static boolean shouldGenerate() {
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
        return player != null && (((PolymerNetworkHandlerExtension) player.networkHandler).polymer_hasResourcePack() || ((player.server.isHost(player.getGameProfile()) && ClientUtils.isResourcePackLoaded())));
    }

    /**
     * Sets resource pack status of player
     *
     * @param player Player to change status
     * @param status true if player has resource pack, otherwise false
     */
    public static void setPlayerStatus(ServerPlayerEntity player, boolean status) {
        ((PolymerNetworkHandlerExtension) player.networkHandler).polymer_setResourcePack(status);
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
    }

    public static boolean shouldCheckByDefault() {
        return DEFAULT_CHECK;
    }

    public static PolymerRPBuilder createBuilder(Path output) {
        return new DefaultRPBuilder(output);
    }

    public static boolean build(Path output) {
        try {
            return INSTANCE.build(output);
        } catch (Exception e) {
            PolymerImpl.LOGGER.error("Couldn't create resource pack!");
            e.printStackTrace();
            return false;
        }
    }

    static {
        INSTANCE.creationEvent.register((builder) -> {
            Path path = PolymerImpl.getGameDir().resolve("polymer-resourcepack-input");
            if (path.toFile().exists()) {
                builder.copyFromPath(path);
            }

            if (CompatStatus.POLYMC) {
                try {
                    Files.createDirectories(path);
                    PolyMcHelpers.createResources("polymc-generated");
                    var polyPath = FabricLoader.getInstance().getGameDir().resolve("polymc-generated").toAbsolutePath();
                    if (polyPath.toFile().exists()) {
                        builder.copyFromPath(polyPath);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }


        });
    }

    public static ResourcePackCreator getInstance() {
        return INSTANCE;
    }
}
