package eu.pb4.polymer.resourcepack.api;

import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.resourcepack.api.model.ItemOverride;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Utilities allowing simple creation of resource pack
 */
public final class ResourcePackCreator {
    public final SimpleEvent<Consumer<ResourcePackBuilder>> creationEvent = new SimpleEvent<>();
    public final SimpleEvent<Runnable> finishedEvent = new SimpleEvent<>();
    public final SimpleEvent<Consumer<ResourcePackBuilder>> afterInitialCreationEvent = new SimpleEvent<>();
    private final Object2IntMap<Identifier> currentCustomModelDataValue = new Object2IntOpenHashMap<>();
    private final Map<Identifier, List<ItemOverride>> modelOverrides = new HashMap<>();

    private final Set<String> modIds = new HashSet<>();
    private final Set<String> modIdsNoCopy = new HashSet<>();
    private final Set<Identifier> bridgedModels = new HashSet<>();
    private Text packDescription = null;
    private byte[] packIcon = null;
    private final Set<Path> sourcePaths = new HashSet<>();

    public static ResourcePackCreator create() {
        return new ResourcePackCreator(0);
    }
    public static ResourcePackCreator createCopy(ResourcePackCreator source, boolean copyEvents) {
        var creator = new ResourcePackCreator(source.currentCustomModelDataValue.defaultReturnValue());
        if (copyEvents) {
            source.creationEvent.invokers().forEach(creator.creationEvent::register);
            source.afterInitialCreationEvent.invokers().forEach(creator.afterInitialCreationEvent::register);
            source.finishedEvent.invokers().forEach(creator.finishedEvent::register);
        }

        creator.currentCustomModelDataValue.putAll(source.currentCustomModelDataValue);
        source.modelOverrides.forEach((a, b) -> creator.modelOverrides.put(a, new ArrayList<>(b)));
        creator.modIds.addAll(source.modIds);
        creator.modIdsNoCopy.addAll(source.modIdsNoCopy);
        creator.packDescription = source.packDescription;
        creator.packIcon = source.packIcon;
        creator.sourcePaths.addAll(source.sourcePaths);

        return creator;
    }

    ResourcePackCreator(int cmdOffset) {
        this.currentCustomModelDataValue.defaultReturnValue(cmdOffset);
    }

    /**
     * This method can be used to register custom model data for models
     *
     * @param model Target model. Requires full path.
     * @param override Override that needs to be added
     * @return Custom model data id.
     */
    public int defineCustomModelData(Identifier model, Identifier override) {
        var id = requestCustomModelDataValue(model);
        defineOverride(model, ItemOverride.of(override, ItemOverride.CUSTOM_MODEL_DATA, id));
        return id;
    }

    /**
     * This method can be used to define custom overrides items
     *
     * @param model Target model. Requires full path.
     * @param override Override that needs to be added
     */
    public void defineOverride(Identifier model, ItemOverride override) {
        this.modelOverrides.computeIfAbsent(model, (x) -> new ArrayList<>()).add(override);
    }

    public int requestCustomModelDataValue(Identifier model) {
        var val = this.currentCustomModelDataValue.getInt(model);
        this.currentCustomModelDataValue.put(model, val + 1);
        return val;
    }

    /**
     * Adds a bridge, allowing you to access any model from selected folder as `namespace:-/modelpath`.
     *
     * @param modelFolderId Model folder to bridge. For example "mod:block" will bridge all models from "assets/mod/models/block"
     * @return Success of addition.
     */
    public boolean addBridgedModelsFolder(Identifier modelFolderId) {
        if (modelFolderId.getPath().equals("item") || modelFolderId.getPath().startsWith("item/")) {
            return false;
        }
        return this.bridgedModels.add(modelFolderId);
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param modId Id of mods used as a source
     */
    public boolean addAssetSource(String modId) {
        if (CommonImpl.isModLoaded(modId)) {
            this.modIds.add(modId);
            return true;
        }

        return false;
    }

    public boolean addAssetSourceWithoutCopy(String modId) {
        if (CommonImpl.isModLoaded(modId)) {
            this.modIdsNoCopy.add(modId);
            return true;
        }

        return false;
    }

    /**
     * Adds mod with provided mod id as a source of assets
     *
     * @param sourcePath Path to a source
     */
    public boolean addAssetSource(Path sourcePath) {
        return this.sourcePaths.add(sourcePath);
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(String description) {
        this.packDescription = Text.literal(description);
    }

    /**
     * Sets pack description
     *
     * @param description new description
     */
    public void setPackDescription(Text description) {
        this.packDescription = description;
    }

    @Nullable
    public Text getPackDescription() {
        return this.packDescription;
    }

    /**
     * Sets icon of pack
     *
     * @param packIcon bytes representing png image of icon
     */
    public void setPackIcon(byte[] packIcon) {
        this.packIcon = packIcon;
    }

    @Nullable
    public byte[] getPackIcon() {
        return packIcon;
    }

    public boolean isEmpty() {
        return this.currentCustomModelDataValue.isEmpty() && this.modIds.isEmpty() && this.creationEvent.isEmpty();
    }

    public boolean build(Path output) throws ExecutionException, InterruptedException {
        return build(output, (s) -> {});
    }

    public boolean build(Path output, Consumer<String> status) throws ExecutionException, InterruptedException {
        boolean successful = true;

        var builder = new DefaultRPBuilder(output);
        status.accept("action:created_builder");

        if (this.packDescription != null) {
            builder.getPackMcMetaBuilder().metadata(new PackResourceMetadata(this.packDescription, SharedConstants.getGameVersion()
                    .getResourceVersion(ResourceType.CLIENT_RESOURCES),
                    Optional.empty()));
        }


        if (this.packIcon != null) {
            builder.addData("pack.png", this.packIcon);
        }

        status.accept("action:creation_event_start");
        this.creationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:creation_event_finish");

        for (var path : this.sourcePaths) {
            status.accept("action:copy_path_start/" + path);
            successful = builder.copyFromPath(path) && successful;
            status.accept("action:copy_path_end/" + path);
        }

        for (String modId : this.modIdsNoCopy) {
            status.accept("action:add_source_mod_start/" + modId);
            successful = builder.addAssetsSource(modId) && successful;
            status.accept("action:add_source_mod_end/" + modId);
        }

        for (String modId : this.modIds) {
            status.accept("action:copy_mod_start/" + modId);
            successful = builder.copyAssets(modId) && successful;
            status.accept("action:copy_mod_end/" + modId);
        }

        status.accept("action:copy_overrides_start");
        for (var entry : this.modelOverrides.entrySet()) {
            var ext = builder.getCustomModels(entry.getKey(), DefaultRPBuilder.OverridePlace.EXISTING);
            var cmd = builder.getCustomModels(entry.getKey(), DefaultRPBuilder.OverridePlace.WITH_CUSTOM_MODEL_DATA);
            entry.getValue().forEach(a -> (a.containsPredicate(ItemOverride.CUSTOM_MODEL_DATA) ? cmd : ext).add(a.toJson()));
        }
        status.accept("action:copy_overrides_finish");

        status.accept("action:late_creation_event_start");
        this.afterInitialCreationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:late_creation_event_finish");

        if (!this.bridgedModels.isEmpty()) {
            builder.addPreFinishTask((b) -> {
                b.forEachFile((path, out) -> {
                    if (!path.startsWith("assets/")) {
                        return;
                    }
                    path = path.substring("assets/".length());

                    for (var x : this.bridgedModels) {
                        var y = x.getNamespace() + "/models/" + x.getPath() + "/";
                        if (path.startsWith(y)) {
                            if (!path.endsWith(".json")) {
                                return;
                            }
                            path = path.substring(x.getNamespace().length() + "/models/".length(), path.length() - ".json".length());
                            b.addData("assets/" + x.getNamespace() + "/models/item/-/" + path + ".json",
                                    ("{\"parent\":\"" + x.getNamespace() + ":" + path + "\"}").getBytes(StandardCharsets.UTF_8));
                            return;
                        }
                    }
                });
            });
        }

        status.accept("action:build");
        successful = builder.buildResourcePack().get() && successful;

        status.accept("action:done");
        this.finishedEvent.invoke(Runnable::run);
        return successful;
    }
}
