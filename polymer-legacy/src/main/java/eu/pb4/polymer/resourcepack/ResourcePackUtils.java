package eu.pb4.polymer.resourcepack;

import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.other.Event;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.*;

/**
 * Use {@link PolymerRPUtils} instead
 */
@Deprecated
public class ResourcePackUtils {
    public static final Event<RPBuilder> RESOURCE_PACK_CREATION_EVENT = new Event<>();

    static {
        PolymerRPUtils.RESOURCE_PACK_CREATION_EVENT.register((builder) -> {
            RESOURCE_PACK_CREATION_EVENT.invoke(new RPBuilder() {
                @Override
                public boolean addData(String path, byte[] data) {
                    return builder.addData(path, data);
                }

                @Override
                public boolean copyModAssets(String modId) {
                    return builder.copyModAssets(modId);
                }

                @Override
                public boolean addCustomModelData(CMDInfo cmdInfo) {
                    return builder.addCustomModelData(cmdInfo.toNew());
                }

                @Override
                public boolean finish() {
                    return false;
                }
            });
        });
    }

    public static CMDInfo requestCustomModelData(Item vanillaItem, Identifier modelPath) {
        return CMDInfo.fromNew(PolymerRPUtils.requestModel(vanillaItem, modelPath));
    }

    public static boolean addModAsAssetsSource(String modId) {
        return PolymerRPUtils.addAssetSource(modId);
    }

    public static boolean shouldGenerate() {
        return PolymerRPUtils.shouldGenerate();
    }

    public static void markAsRequired() {
        PolymerRPUtils.markAsRequired();
    }

    public static boolean isRequired() {
        return PolymerRPUtils.isRequired();
    }

    public static boolean hasPack(ServerPlayerEntity player) {
        return PolymerRPUtils.hasPack(player);
    }

    public static void setPlayerStatus(ServerPlayerEntity player, boolean status) {
        PolymerRPUtils.setPlayerStatus(player, status);
    }

    public static List<CMDInfo> getModelsFor(Item item) {
        return CMDInfo.fromNew(PolymerRPUtils.getModelsFor(item));
    }

    public static void disableDefaultCheck() {
        PolymerRPUtils.disableDefaultCheck();
    }

    public static boolean shouldCheckByDefault() {
        return PolymerRPUtils.shouldCheckByDefault();
    }

    public static boolean build(Path path) {
        return false;
    }
}
