package eu.pb4.blocktest;

import eu.pb4.polymer.api.item.PolymerItemGroup;
import eu.pb4.polymer.api.resourcepack.PolymerRPUtils;
import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class TestInitializer implements ModInitializer {
    public static final PolymerItemGroup ITEM_GROUP = PolymerItemGroup.create(new Identifier("test/textured_blocks"), Text.literal("Textured blocks"), () -> new ItemStack(Items.BAMBOO));

    @Override
    public void onInitialize() {
        PolymerRPUtils.markAsRequired();
        PolymerRPUtils.addAssetSource("apolymertestblocks");

        register(BlockModelType.FULL_BLOCK, "block/chlorophyte_block");
        register(BlockModelType.TRANSPARENT_BLOCK, "block/chair");
        register(BlockModelType.FARMLAND_BLOCK, "block/copper_block");
        register(BlockModelType.VINES_BLOCK, "block/table");
        register(BlockModelType.BIOME_PLANT_BLOCK, "block/steel_block");
        register(BlockModelType.KELP_BLOCK, "block/titan_ore_nether");
    }

    public static void register(BlockModelType type, String modelId) {
        var id = new Identifier("blocktest", modelId);
        var block = Registry.register(Registry.BLOCK, id,
                new TestBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), type, modelId));

        Registry.register(Registry.ITEM, id, new TestItem(new Item.Settings().group(ITEM_GROUP), block, modelId));
    }
}
