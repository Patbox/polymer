package eu.pb4.polymer.api.resourcepack;

import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.item.Item;
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
    public final SimpleEvent<Consumer<PolymerRPBuilder>> creationEvent = new SimpleEvent<>();
    public final SimpleEvent<Consumer<PolymerRPBuilder>> afterInitialCreationEvent = new SimpleEvent<>();
    private final Map<Item, List<PolymerModelData>> items = new Object2ObjectOpenHashMap<>();
    private final Map<Item, Map<Identifier, PolymerModelData>> itemsMap = new Object2ObjectOpenHashMap<>();
    private final Set<String> modIds = new HashSet<>();
    private final IntSet takenArmorColors = new IntOpenHashSet();
    private final Map<Identifier, PolymerArmorModel> armorModelMap = new HashMap<>();
    private final int cmdOffset;
    private int armorColor = 0;
    private String packDescription = null;
    private byte[] packIcon = null;

    public static ResourcePackCreator create() {
        return new ResourcePackCreator(0);
    }

    protected ResourcePackCreator(int cmdOffset) {
        this.cmdOffset = cmdOffset;
    }

    /**
     * This method can be used to register custom model data for items
     *
     * @param vanillaItem Vanilla/Client side item
     * @param modelPath   Path to model in resource pack
     * @return PolymerModelData with data about this model
     */
    public PolymerModelData requestModel(Item vanillaItem, Identifier modelPath) {
        var map = this.itemsMap.computeIfAbsent(vanillaItem, (x) -> new Object2ObjectOpenHashMap<>());

        if (map.containsKey(modelPath)) {
            return map.get(modelPath);
        } else {
            var cmdInfoList = this.items.computeIfAbsent(vanillaItem, (x) -> new ArrayList<>());

            var cmdInfo = new PolymerModelData(vanillaItem, cmdInfoList.size() + this.cmdOffset, modelPath);
            cmdInfoList.add(cmdInfo);
            map.put(modelPath, cmdInfo);
            return cmdInfo;
        }
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
            int color = 0xFFFFFF - armorColor * 2;
            var model = new PolymerArmorModel(color, modelPath);

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
        if (PolymerImpl.isModLoaded(modId)) {
            modIds.add(modId);
            return true;
        }

        return false;
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
        this.packDescription = description;
    }

    @Nullable
    public String getPackDescription() {
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
        return this.items.values().size() <= 0 || this.modIds.size() <= 0 || this.armorModelMap.size() <= 0;
    }

    public boolean build(Path output) throws ExecutionException, InterruptedException {
        return build(output, (s) -> {});
    }

    public boolean build(Path output, Consumer<String> status) throws ExecutionException, InterruptedException {
        boolean successful = true;

        var builder = new DefaultRPBuilder(output);
        status.accept("action:created_builder");

        if (this.packDescription != null) {
            builder.addData("pack.mcmeta", ("" +
                    "{\n" +
                    "   \"pack\":{\n" +
                    "      \"pack_format\":" + SharedConstants.RESOURCE_PACK_VERSION + ",\n" +
                    "      \"description\":\"Server resource pack\"\n" +
                    "   }\n" +
                    "}\n").getBytes(StandardCharsets.UTF_8));
        }


        if (this.packIcon != null) {
            builder.addData("pack.png", this.packIcon);
        }

        status.accept("action:creation_event_start");
        this.creationEvent.invoke((x) -> x.accept(builder));
        status.accept("action:creation_event_finish");

        for (String modId : this.modIds) {
            status.accept("action:copy_mod_start/" + modId);
            successful = builder.copyModAssets(modId) && successful;
            status.accept("action:copy_mod_end/" + modId);

        }
        
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
        return successful;
    }
}
