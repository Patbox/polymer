package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TestInitializer implements ModInitializer {
    //public static final PolymerItemGroupUtils ITEM_GROUP = PolymerItemGroupUtils.create(new Identifier("test/textured_blocks"), Text.literal("Textured blocks"), () -> new ItemStack(Items.BAMBOO));

    @Override
    public void onInitialize() {
        PolymerResourcePackUtils.markAsRequired();
        PolymerResourcePackUtils.addModAssets("apolymertestblocks");

        register(BlockModelType.FULL_BLOCK, "block/chlorophyte_block");
        register(BlockModelType.TRANSPARENT_BLOCK, "block/chair");
        register(BlockModelType.FARMLAND_BLOCK, "block/copper_block");
        register(BlockModelType.VINES_BLOCK, "block/table");
        register(BlockModelType.BIOME_PLANT_BLOCK, "block/steel_block");
        register(BlockModelType.KELP_BLOCK, "block/titan_ore_nether");
    }

    public static void register(BlockModelType type, String modelId) {
        var id = new Identifier("blocktest", modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new TestBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), type, modelId));

        Registry.register(Registries.ITEM, id, new TestItem(new Item.Settings(), block, modelId));
    }
}
