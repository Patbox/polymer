package eu.pb4.polymer.resourcepack.api;

import com.google.gson.JsonObject;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.resourcepack.api.model.ItemOverride;
import eu.pb4.polymer.resourcepack.impl.generation.DefaultRPBuilder;
import eu.pb4.polymer.resourcepack.impl.generation.PolymerArmorModelImpl;
import eu.pb4.polymer.resourcepack.impl.generation.PolymerModelDataImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
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
    private final Map<Item, List<PolymerModelData>> items = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);
    private final Object2IntMap<Item> itemIds = new Object2IntOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);
    private final Map<Item, Map<Identifier, PolymerModelData>> itemModels = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);
    private final Map<Item, List<ItemOverride>> itemOverrides = new Object2ObjectOpenCustomHashMap<>(CommonImplUtils.IDENTITY_HASH);

    private final Set<String> modIds = new HashSet<>();
    private final Set<String> modIdsNoCopy = new HashSet<>();
    private final IntSet takenArmorColors = new IntOpenHashSet();
    private final Map<Identifier, PolymerArmorModel> armorModelMap = new HashMap<>();
    private final int cmdOffset;
    private int armorColor = 0;
    private Text packDescription = null;
    private byte[] packIcon = null;
    private Set<Path> sourcePaths = new HashSet<>();

    public static ResourcePackCreator create() {
        return new ResourcePackCreator(0);
    }

    protected ResourcePackCreator(int cmdOffset) {
        this.cmdOffset = cmdOffset;
        this.itemIds.defaultReturnValue(1);
    }

    /**
     * This method can be used to register custom model data for items
     *
     * @param vanillaItem Vanilla/Client side item
     * @param modelPath   Path to model in resource pack
     * @return PolymerModelData with data about this model
     */
    public PolymerModelData requestModel(Item vanillaItem, Identifier modelPath) {
        var map = this.itemModels.computeIfAbsent(vanillaItem, (x) -> new Object2ObjectOpenHashMap<>());

        if (map.containsKey(modelPath)) {
            return map.get(modelPath);
        } else {
            return this.forceDefineModel(vanillaItem, this.itemIds.getInt(vanillaItem), modelPath, true);
        }
    }

    /**
     * This method can be used to define custom overrides items
     *
     * @param item Vanilla/Client side item
     * @param override Override that needs to be added
     * @return PolymerModelData with data about this model
     */
    public void defineOverride(Item item, ItemOverride override) {
        this.itemOverrides.computeIfAbsent(item, (x) -> new ArrayList<>()).add(override);
    }

    /**
     * This method can be used to register custom model data for items.
     * Use this method only if you really need to preserve ids
     *
     * @param vanillaItem Vanilla/Client side item
     * @param customModelData forced CMD
     * @param modelPath   Path to model in resource pack
     * @param respectOffset Whatever output CustomModelData should have offset added
     * @return PolymerModelData with data about this model
     */
    @ApiStatus.Experimental
    public PolymerModelData forceDefineModel(Item vanillaItem, int customModelData, Identifier modelPath, boolean respectOffset) {
        var map = this.itemModels.computeIfAbsent(vanillaItem, (x) -> new Object2ObjectOpenHashMap<>());

        var cmdInfoList = this.items.computeIfAbsent(vanillaItem, (x) -> new ArrayList<>());
        var cmdInfo = new PolymerModelDataImpl(vanillaItem, customModelData + (respectOffset ? this.cmdOffset : 0), modelPath);
        cmdInfoList.add(cmdInfo);
        this.itemIds.put(vanillaItem, Math.max(this.itemIds.getInt(vanillaItem), customModelData + 1));
        map.put(modelPath, cmdInfo);
        return cmdInfo;
    }

    /**
     * This method can be used to register custom model data for items
     *
     * @param modelPath Path to model in resource pack
     * @return PolymerArmorModel with data about this model
     */
    public PolymerArmorModel requestArmor(Identifier modelPath) {
        if (this.armorModelMap.containsKey(modelPath)) {
            return this.armorModelMap.get(modelPath);
        } else {
            this.armorColor++;
            int color = 0xFFFFFF - armorColor * 2 + 1;
            var model = new PolymerArmorModelImpl(color, modelPath);

            this.armorModelMap.put(modelPath, model);
            this.takenArmorColors.add(color);
            return model;
        }
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
     * Returns true if color is taken
     */
    public boolean isColorTaken(int color) {
        return takenArmorColors.contains(color & 0xFFFFFF);
    }

    /**
     * Gets an unmodifiable list of models for an item.
     * This can be useful if you need to extract this list and parse it yourself.
     *
     * @param item Item you want list for
     * @return An unmodifiable list of models
     */
    public List<PolymerModelData> getModelsFor(Item item) {
        return Collections.unmodifiableList(items.getOrDefault(item, Collections.emptyList()));
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
        return this.items.isEmpty() && this.modIds.isEmpty() && this.armorModelMap.isEmpty() && this.creationEvent.isEmpty();
    }

    public boolean build(Path output) throws ExecutionException, InterruptedException {
        return build(output, (s) -> {});
    }

    public boolean build(Path output, Consumer<String> status) throws ExecutionException, InterruptedException {
        boolean successful = true;

        var builder = new DefaultRPBuilder(output);
        status.accept("action:created_builder");

        if (this.packDescription != null) {
            var obj = new JsonObject();

            var pack = new JsonObject();
            pack.addProperty("pack_format", SharedConstants.RESOURCE_PACK_VERSION);
            pack.add("description", Text.Serialization.toJsonTree(this.packDescription));

            obj.add("pack", pack);
            builder.addData("pack.mcmeta", obj.toString().getBytes(StandardCharsets.UTF_8));
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
        for (var entry : this.itemOverrides.entrySet()) {
            var x = builder.getCustomModels(entry.getKey(), DefaultRPBuilder.OverridePlace.BEFORE_CUSTOM_MODEL_DATA);
            entry.getValue().forEach(a -> x.add(a.toJson()));
        }
        status.accept("action:copy_overrides_finish");
        
        for (var cmdInfoList : this.items.values()) {
            status.accept("action:custom_model_data_start");
            for (PolymerModelData cmdInfo : cmdInfoList) {
                builder.addCustomModelData(cmdInfo);
            }
            status.accept("action:add_custom_model_data_finish");
        }

        status.accept("action:custom_armors_start");
        for (var armor : this.armorModelMap.values()) {
            builder.addArmorModel(armor);
        }
        status.accept("action:custom_armors_finish");

        status.accept("action:late_creation_event_start");
        this.afterInitialCreationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:late_creation_event_finish");

        status.accept("action:build");
        successful = builder.buildResourcePack().get() && successful;

        status.accept("action:done");
        this.finishedEvent.invoke(Runnable::run);
        return successful;
    }
}
