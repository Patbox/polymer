package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.MultiPolymerBlockModel;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

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
        register(BlockModelType.TOP_SLAB, "block/chlorophyte_ore");
        register(BlockModelType.TRIPWIRE_BLOCK, "block/titan_block");
        register(BlockModelType.VINES_BLOCK, "block/table");
        register(BlockModelType.BIOME_PLANT_BLOCK, "block/steel_block");
        register(BlockModelType.KELP_BLOCK, "block/titan_ore_nether");
        registerHead(BlockModelType.HEAD, "block/table2");

        registerMulti(BlockModelType.CAMPFIRE,"multi_variants_base", MultiPolymerBlockModel.of()
                .with(Identifier.ofVanilla("block/redstone_torch"))
                .with(Identifier.ofVanilla("block/polished_tuff_slab"))
        );

        registerMulti(BlockModelType.WEST_SHELF,"multi_multi_base", MultiPolymerBlockModel.of()
                .with(Identifier.ofVanilla("block/torch"))
                .with(Identifier.ofVanilla("block/polished_tuff_slab"))
        );


        for (var model : BlockModelType.values()) {
            registerEmpty(model);
        }
    }

    public static void register(BlockModelType type, String modelId) {
        var id = Identifier.of("blocktest", modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new TestBlock(Block.Settings.copy(Blocks.DIAMOND_BLOCK).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), type, modelId));

        Registry.register(Registries.ITEM, id, new TestItem(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id)),
                block, modelId));
    }

    public static void registerHead(BlockModelType type, String modelId) {
        var id = Identifier.of("blocktest", modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new TestHeadBlock(Block.Settings.copy(Blocks.DIAMOND_BLOCK).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), type, modelId));

        Registry.register(Registries.ITEM, id, new TestItem(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id)),
                block, modelId));
    }

    public static void registerMulti(BlockModelType type, String modelId, MultiPolymerBlockModel model) {
        var id = Identifier.of("blocktest", modelId);
        var block = Registry.register(Registries.BLOCK, id,
                new TestMultiBlock(Block.Settings.copy(Blocks.DIAMOND_BLOCK).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), type, model));

        Registry.register(Registries.ITEM, id, new TestItem(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id)),
                block, modelId));
    }

    public static void registerEmpty(BlockModelType type) {
        var id = Identifier.of("blocktest", "empty/" + type.name().toLowerCase(Locale.ROOT));
        var block = Registry.register(Registries.BLOCK, id,
                new EmptyBlock(Block.Settings.copy(Blocks.DIAMOND_BLOCK).registryKey(RegistryKey.of(RegistryKeys.BLOCK, id)), type));

        Registry.register(Registries.ITEM, id, new PolymerBlockItem(block, new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                        //.modelId(block.getPolymerBlockState(block.getDefaultState(), PacketContext.create()).getBlock().getRegistryEntry().registryKey().getValue())
                        .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true),
                block.getPolymerBlockState(block.getDefaultState(), PacketContext.create()).getBlock().asItem()));
    }
}
