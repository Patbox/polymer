package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class TestInitializer implements ModInitializer {
    //public static final PolymerItemGroupUtils ITEM_GROUP = PolymerItemGroupUtils.create(Identifier.of("test/textured_blocks"), Text.literal("Textured blocks"), () -> new ItemStack(Items.BAMBOO));

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
        registerEmpty(BlockModelType.KELP_BLOCK);
        registerEmpty(BlockModelType.VINES_BLOCK);
        registerEmpty(BlockModelType.PLANT_BLOCK);
    }

    public static void register(BlockModelType type, String modelId) {
        var id = Identifier.of("blocktest", modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new TestBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), type, modelId));

        Registry.register(Registries.ITEM, id, new TestItem(new Item.Settings(), block, modelId));
    }

    public static void registerEmpty(BlockModelType type) {
        var id = Identifier.of("blocktest", "empty/" + type.name().toLowerCase(Locale.ROOT));
        var block = Registry.register(Registries.BLOCK, id,
                new EmptyBlock(FabricBlockSettings.copy(Blocks.DIAMOND_BLOCK), type));

        Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new Item.Settings()
                .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true),
                block.getPolymerBlockState(block.getDefaultState()).getBlock().asItem()));
    }
}
