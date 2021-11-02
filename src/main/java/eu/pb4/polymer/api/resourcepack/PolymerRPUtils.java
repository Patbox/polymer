package eu.pb4.polymer.api.resourcepack;

import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerMod;
import eu.pb4.polymer.impl.client.ClientUtils;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utilities allowing creation of single, polymer mod compatible resource pack
 */
public class PolymerRPUtils {
    public static final SimpleEvent<Consumer<PolymerRPBuilder>> RESOURCE_PACK_CREATION_EVENT = new SimpleEvent<>();
    private static final Object2ObjectMap<Item, List<PolymerModelData>> ITEMS = new Object2ObjectArrayMap<>();
    private static final Set<String> MOD_IDS = new HashSet<>();
    private static final int CMD_OFFSET = PolymerMod.POLYMC_COMPAT ? 100000 : 1;
    private static boolean REQUIRED = false;
    private static boolean DEFAULT_CHECK = true;

    /**
     * This method can be used to register custom model data for items
     *
     * @param vanillaItem Vanilla/Client side item
     * @param modelPath   Path to model in resource pack
     * @return PolymerModelData with data about this model
     */
    public static PolymerModelData requestModel(Item vanillaItem, Identifier modelPath) {
        List<PolymerModelData> cmdInfoList = ITEMS.get(vanillaItem);
        if (cmdInfoList == null) {
            cmdInfoList = new ArrayList<>();
            ITEMS.put(vanillaItem, cmdInfoList);
        }

        PolymerModelData cmdInfo = new PolymerModelData(vanillaItem, cmdInfoList.size() + CMD_OFFSET, modelPath);
        cmdInfoList.add(cmdInfo);
        return cmdInfo;
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public static boolean addAssetSource(String modId) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            MOD_IDS.add(modId);
            return true;
        }

        return false;
    }

    /**
     * Allows to check if there are any provided resources
     */
    public static boolean shouldGenerate() {
        return ITEMS.values().size() > 0 || MOD_IDS.size() > 0;
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
    public static boolean hasPack(ServerPlayerEntity player) {
        return ((PolymerNetworkHandlerExtension) player.networkHandler).polymer_hasResourcePack() || ((player.server.isHost(player.getGameProfile()) && ClientUtils.isResourcePackLoaded()));
    }

    public static void setPlayerStatus(ServerPlayerEntity player, boolean status) {
        ((PolymerNetworkHandlerExtension) player.networkHandler).polymer_setResourcePack(status);
    }

    /**
     * Gets an unmodifiable list of models for an item.
     * This can be useful if you need to extract this list and parse it yourself.
     *
     * @param item Item you want list for
     * @return An unmodifiable list of models
     */
    public static List<PolymerModelData> getModelsFor(Item item) {
        return Collections.unmodifiableList(ITEMS.getOrDefault(item, Collections.emptyList()));
    }

    public static void disableDefaultCheck() {
        DEFAULT_CHECK = false;
    }

    public static boolean shouldCheckByDefault() {
        return DEFAULT_CHECK;
    }

    public static boolean build(Path output) {
        try {
            boolean successful = true;

            Path possibleInput = FabricLoader.getInstance().getGameDir().resolve("polymer-resourcepack-input");

            var builder = new DefaultRPBuilder(output);

            RESOURCE_PACK_CREATION_EVENT.invoke((x) -> x.accept(builder));

            for (String modId : MOD_IDS) {
                successful = builder.copyModAssets(modId) && successful;
            }

            if (possibleInput.toFile().exists()) {
                builder.copyFromPath(possibleInput);
            }

            for (List<PolymerModelData> cmdInfoList : ITEMS.values()) {
                for (PolymerModelData cmdInfo : cmdInfoList) {
                    successful = builder.addCustomModelData(cmdInfo) && successful;
                }
            }

            successful = builder.buildResourcePack().get() && successful;

            return successful;
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Couldn't create resource pack!");
            e.printStackTrace();
            return false;
        }
    }
}
