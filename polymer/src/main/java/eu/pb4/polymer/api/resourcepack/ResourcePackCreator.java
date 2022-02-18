package eu.pb4.polymer.api.resourcepack;

import eu.pb4.polymer.api.utils.events.SimpleEvent;
import eu.pb4.polymer.impl.PolymerImpl;
import eu.pb4.polymer.impl.resourcepack.DefaultRPBuilder;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
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
    private final Object2ObjectMap<Item, List<PolymerModelData>> items = new Object2ObjectArrayMap<>();
    private final Set<String> modIds = new HashSet<>();
    private final IntSet takenArmorColors = new IntOpenHashSet();
    private final Map<Identifier, PolymerArmorModel> armorModelMap = new HashMap<>();
    private final int cmdOffset;
    private int armorColor = 0;
    private String packDescription = null;
    private byte[] packIcon = null;

    public static final ResourcePackCreator create() {
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
        List<PolymerModelData> cmdInfoList = items.get(vanillaItem);
        if (cmdInfoList == null) {
            cmdInfoList = new ArrayList<>();
            items.put(vanillaItem, cmdInfoList);
        }

        PolymerModelData cmdInfo = new PolymerModelData(vanillaItem, cmdInfoList.size() + cmdOffset, modelPath);
        cmdInfoList.add(cmdInfo);
        return cmdInfo;
    }

    /**
     * This method can be used to register custom model data for items
     *
     * @param modelPath Path to model in resource pack
     * @return PolymerArmorModel with data about this model
     */
    public PolymerArmorModel requestArmor(Identifier modelPath) {
        if (armorModelMap.containsKey(modelPath)) {
            return armorModelMap.get(modelPath);
        } else {
            armorColor++;
            int color = 0xFFFFFF - armorColor * 2;
            var model = new PolymerArmorModel(color, modelPath);

            armorModelMap.put(modelPath, model);
            takenArmorColors.add(color);
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
        boolean successful = true;

        var builder = new DefaultRPBuilder(output);


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

        this.creationEvent.invoke((x) -> x.accept(builder));

        for (String modId : this.modIds) {
            successful = builder.copyModAssets(modId) && successful;
        }
        
        for (var cmdInfoList : this.items.values()) {
            for (PolymerModelData cmdInfo : cmdInfoList) {
                builder.addCustomModelData(cmdInfo);
            }
        }

        for (var armor : this.armorModelMap.values()) {
            builder.addArmorModel(armor);
        }

        successful = builder.buildResourcePack().get() && successful;

        return successful;
    }
}
