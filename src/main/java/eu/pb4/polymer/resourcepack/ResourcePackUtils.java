package eu.pb4.polymer.resourcepack;

import com.google.gson.JsonParser;
import eu.pb4.polymer.PolymerMod;
import eu.pb4.polymer.other.Event;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.*;

public class ResourcePackUtils {
    static final JsonParser JSON_PARSER = new JsonParser();

    private static final Object2ObjectMap<Item, List<CMDInfo>> ITEMS = new Object2ObjectArrayMap<>();
    private static final Set<String> MOD_IDS = new HashSet<>();

    private static int CMD_OFFSET = PolymerMod.POLYMC_COMPAT ? 100000 : 1;

    /**
     * This method can be used to register custom model data for items
     *
     * @param vanillaItem Vanilla/Client side item
     * @param modelPath   Path to model in resource pack
     * @return CMDInfo with data about this model
     */
    public static CMDInfo requestCustomModelData(Item vanillaItem, Identifier modelPath) {
        List<CMDInfo> cmdInfoList = ITEMS.get(vanillaItem);
        if (cmdInfoList == null) {
            cmdInfoList = new ArrayList<>();
            ITEMS.put(vanillaItem, cmdInfoList);
        }

        CMDInfo cmdInfo = new CMDInfo(vanillaItem, cmdInfoList.size() + CMD_OFFSET, modelPath);
        cmdInfoList.add(cmdInfo);
        return cmdInfo;
    }


    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public static boolean addModAsAssetsSource(String modId) {
        if (FabricLoader.getInstance().isModLoaded(modId)) {
            MOD_IDS.add(modId);
            return true;
        }

        return false;
    }

    @ApiStatus.Internal
    public static boolean build(Path path) {
        try {
            PolymerMod.LOGGER.info("Starting resource pack creation...");
            boolean successful = true;
            RPBuilder builder = new DefaultRPBuilder(path);

            RESOURCE_PACK_CREATION_EVENT.invoke(builder);

            for (String modId : MOD_IDS) {
                successful = builder.copyModAssets(modId) && successful;
            }

            for (List<CMDInfo> cmdInfoList : ITEMS.values()) {
                for (CMDInfo cmdInfo : cmdInfoList) {
                    successful = builder.addCustomModelData(cmdInfo) && successful;
                }
            }

            successful = builder.finish() && successful;

            if (successful) {
                PolymerMod.LOGGER.info("Resource pack created successfully! You can find it in: " + path);
            } else {
                PolymerMod.LOGGER.warn("Found issues while creating resource pack! See logs above for more detail!");
            }

            return successful;
        } catch (Exception e) {
            PolymerMod.LOGGER.error("Couldn't create resource pack!");
            e.printStackTrace();
            return false;
        }
    }

    public static final Event<RPBuilder> RESOURCE_PACK_CREATION_EVENT = new Event<>();
}
