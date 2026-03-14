package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.MultiPolymerBlockModel;
import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.Locale;
import java.util.function.BiFunction;

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
        registerCustom("block/table2", (s, id) -> new TestHeadBlock(s, BlockModelType.HEAD, id));
        registerCustom("block/oak_fence", (s, id) -> new TestBarsBlock(s));

        registerMulti(BlockModelType.CAMPFIRE,"multi_variants_base", MultiPolymerBlockModel.of()
                .with(Identifier.withDefaultNamespace("block/redstone_torch"))
                .with(Identifier.withDefaultNamespace("block/polished_tuff_slab"))
        );

        registerMulti(BlockModelType.WEST_SHELF,"multi_multi_base", MultiPolymerBlockModel.of()
                .with(Identifier.withDefaultNamespace("block/torch"))
                .with(Identifier.withDefaultNamespace("block/polished_tuff_slab"))
        );


        for (var model : BlockModelType.values()) {
            registerEmpty(model);
        }


        /*var str = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            str.append("BARS");

            var noDirs = true;
            for (var dir : Direction.Type.HORIZONTAL) {
               if (((i >> (dir.getIndex() - 1)) & 1) == 1) {
                    noDirs = false;
                    str.append("_").append(dir.name().toUpperCase(Locale.ROOT));
               }
            }
            if (noDirs) {
                str.append("_CENTER");
            }


            if ((i & 1) == 1) {
                str.append("_WATERLOGGED");
            }

            str.append(",\n");
        }
        System.out.println(str);*/

        /*var str = new StringBuilder();
        var bools = new boolean[] { false, true };
        for (var dir : Direction.Type.HORIZONTAL) {
            for (var half : BlockHalf.values()) {
                for (var shape : StairShape.values()) {
                    for (var waterlogged : bools) {
                        var self = new StringBuilder();
                        self.append("STAIRS_").append(dir.name())
                                .append("_").append(half.name())
                                .append("_").append(shape.name());


                        if (waterlogged) {
                            self.append("_WATERLOGGED");
                        }
                        str.append(self);
                        str.append(",\n");
                    }
                }
            }
        }
        System.out.println(str);*/
    }

    public static void register(BlockModelType type, String modelId) {
        var id = Identifier.fromNamespaceAndPath("blocktest", modelId);
        var block = Registry.register(BuiltInRegistries.BLOCK, id,
                new TestBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)), type, modelId));

        Registry.register(BuiltInRegistries.ITEM, id, new TestItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)),
                block, modelId));
    }

    public static void registerCustom(String baseId, BiFunction<BlockBehaviour.Properties, String, Block> func) {
        var id = Identifier.fromNamespaceAndPath("blocktest", baseId);
        var block = Registry.register(BuiltInRegistries.BLOCK, id,
               func.apply(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)), baseId));

        Registry.register(BuiltInRegistries.ITEM, id, new TestItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)),
                block, baseId));
    }

    public static void registerMulti(BlockModelType type, String modelId, MultiPolymerBlockModel model) {
        var id = Identifier.fromNamespaceAndPath("blocktest", modelId);
        var block = Registry.register(BuiltInRegistries.BLOCK, id,
                new TestMultiBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)), type, model));

        Registry.register(BuiltInRegistries.ITEM, id, new TestItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id)),
                block, modelId));
    }

    public static void registerEmpty(BlockModelType type) {
        var id = Identifier.fromNamespaceAndPath("blocktest", "empty/" + type.name().toLowerCase(Locale.ROOT));
        var block = Registry.register(BuiltInRegistries.BLOCK, id,
                new EmptyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).setId(ResourceKey.create(Registries.BLOCK, id)), type));

        Registry.register(BuiltInRegistries.ITEM, id, new PolymerBlockItem(block, new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                        //.modelId(block.getPolymerBlockState(block.getDefaultState(), PacketContext.create()).getBlock().getRegistryEntry().registryKey().getValue())
                        .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true),
                block.getPolymerBlockState(block.defaultBlockState(), null).getBlock().asItem()));
    }
}
