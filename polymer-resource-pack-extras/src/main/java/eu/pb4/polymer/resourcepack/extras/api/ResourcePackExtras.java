package eu.pb4.polymer.resourcepack.extras.api;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator;
import eu.pb4.polymer.resourcepack.extras.api.format.item.ItemAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.item.model.BasicItemModel;
import eu.pb4.polymer.resourcepack.extras.api.format.item.tint.CustomModelDataTintSource;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import net.minecraft.resources.Identifier;

/**
 * Utilities allowing simple creation of resource pack
 */
public final class ResourcePackExtras {
    private static final ResourcePackExtras MAIN = new ResourcePackExtras(PolymerResourcePackUtils.getInstance());
    private final Map<Identifier, BiFunction<Identifier, ResourcePackBuilder, @Nullable ItemAsset>> bridgedModels = new HashMap<>();

    ResourcePackExtras(ResourcePackCreator creator) {
        creator.afterInitialCreationEvent.register(this::setup);
    }

    public static ResourcePackExtras forDefault() {
        return MAIN;
    }

    public static ResourcePackExtras of(ResourcePackCreator creator) {
        if (PolymerResourcePackUtils.getInstance() == creator) {
            return MAIN;
        }
        return new ResourcePackExtras(creator);
    }

    public static ResourcePackExtras of(ResourcePackCreator creator, ResourcePackExtras source) {
        if (PolymerResourcePackUtils.getInstance() == creator) {
            throw new IllegalArgumentException("Passed creator matches PolymerResourcePackUtils.getInstance(), so you can't copy settings from other sources to it!");
        }

        var extras = new ResourcePackExtras(creator);
        extras.bridgedModels.putAll(source.bridgedModels);
        return extras;
    }

    public static Identifier bridgeModelNoItem(Identifier model) {
        if (model.getPath().startsWith("item/")) {
            return model.withPath(model.getPath().substring("item/".length()));
        }

        return bridgeModel(model);
    }

    public static Identifier bridgeModel(Identifier model) {
        return model.withPrefix("-/");
    }

    /**
     * Adds a bridge, allowing you to access any model from selected folder as `namespace:-/modelpath`.
     *
     * @param modelFolderId ModelAsset folder to bridge. For example "mod:block" will bridge all models from "assets/mod/models/block"
     * @return Success of addition.
     */
    public boolean addBridgedModelsFolder(Identifier modelFolderId) {
        return addBridgedModelsFolder(modelFolderId,
                (identifier, resourcePackBuilder) -> new ItemAsset(new BasicItemModel(identifier), ItemAsset.Properties.DEFAULT)
        );
    }

    public boolean addBridgedModelsFolderWithColor(Identifier modelFolderId) {
        return addBridgedModelsFolder(modelFolderId,
                (identifier, resourcePackBuilder) -> new ItemAsset(new BasicItemModel(identifier,
                        List.of(
                                new CustomModelDataTintSource(0, 0xFFFFFF),
                                new CustomModelDataTintSource(1, 0xFFFFFF),
                                new CustomModelDataTintSource(2, 0xFFFFFF),
                                new CustomModelDataTintSource(3, 0xFFFFFF))
                ), ItemAsset.Properties.DEFAULT)
        );
    }

    public boolean addBridgedModelsFolder(Identifier modelFolderId, BiFunction<Identifier, ResourcePackBuilder, @Nullable ItemAsset> bridgeBuilder) {
        this.bridgedModels.put(modelFolderId.getPath().endsWith("/") ? modelFolderId.withPath(x -> x.substring(0, x.length() - 1)) : modelFolderId, bridgeBuilder);
        return true;
    }

    public boolean addBridgedModelsFolder(Identifier... modelFolderId) {
        var b = true;
        for (var model : modelFolderId) {
            b &= addBridgedModelsFolder(model);
        }
        return b;
    }

    public boolean addBridgedModelsFolder(Collection<Identifier> modelFolderId) {
        var b = true;
        for (var model : modelFolderId) {
            b &= addBridgedModelsFolder(model);
        }
        return b;
    }

    public boolean addBridgedModelsFolder(Collection<Identifier> modelFolderId, BiFunction<Identifier, ResourcePackBuilder, @Nullable ItemAsset> bridgeBuilder) {
        var b = true;
        for (var model : modelFolderId) {
            b &= addBridgedModelsFolder(model, bridgeBuilder);
        }
        return b;
    }


    private void setup(ResourcePackBuilder builder) {
        if (!this.bridgedModels.isEmpty()) {
            builder.addPreFinishTask((b) -> {
                b.forEachResource((path, out) -> {
                    if (!path.startsWith("assets/")) {
                        return;
                    }
                    var originalPath = path;
                    path = path.substring("assets/".length());

                    for (var x : this.bridgedModels.entrySet()) {
                        var key = x.getKey();
                        var y = key.getNamespace() + "/models/" + key.getPath() + "/";
                        if (path.startsWith(y)) {
                            if (!path.endsWith(".json")) {
                                return;
                            }
                            try {
                                path = path.substring(key.getNamespace().length() + "/models/".length(), path.length() - ".json".length());

                                var assetPath = AssetPaths.itemAsset(key.withPath("-/" + path));

                                if (b.getData(assetPath) != null) {
                                    return;
                                }

                                var asset = x.getValue().apply(key.withPath(path), b);
                                if (asset != null) {
                                    b.addData(assetPath, asset.toJson().getBytes(StandardCharsets.UTF_8));
                                }
                                return;
                            } catch (Throwable e) {
                                CommonImpl.LOGGER.warn("Invalid resource pack file! {}", originalPath, e);
                            }
                        }
                    }
                });
            });
        }
    }
}
