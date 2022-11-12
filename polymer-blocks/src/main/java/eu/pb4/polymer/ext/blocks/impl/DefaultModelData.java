package eu.pb4.polymer.ext.blocks.impl;

import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import net.minecraft.block.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultModelData {
    public static final Map<BlockModelType, List<BlockState>> USABLE_STATES = new HashMap<>();
    public static final Map<BlockState, BlockState> SPECIAL_REMAPS = new HashMap<>();
    public static final Map<BlockState, PolymerBlockModel[]> MODELS = new HashMap<>();

    private static final Predicate<BlockState> WATERLOGGED_PREDICATE = (state -> state.getBlock() instanceof Waterloggable && state.get(Properties.WATERLOGGED));
    private static final Predicate<BlockState> NOT_WATERLOGGED_PREDICATE = (state -> !(state.getBlock() instanceof Waterloggable && state.get(Properties.WATERLOGGED)));

    static {
        generateDefault(BlockModelType.FULL_BLOCK, Blocks.NOTE_BLOCK);
        generateDefault(BlockModelType.BIOME_TRANSPARENT_BLOCK, NOT_WATERLOGGED_PREDICATE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        generateDefault(BlockModelType.BIOME_TRANSPARENT_BLOCK_WATERLOGGED, WATERLOGGED_PREDICATE, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        generateDefault(BlockModelType.TRANSPARENT_BLOCK, NOT_WATERLOGGED_PREDICATE, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES);
        generateDefault(BlockModelType.TRANSPARENT_BLOCK_WATERLOGGED, WATERLOGGED_PREDICATE, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES);
        generateDefault(BlockModelType.KELP_BLOCK, Blocks.KELP);
        generateDefault(BlockModelType.CACTUS_BLOCK, Blocks.CACTUS);

        {
            var farmland = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier("minecraft:block/farmland"))};
            MODELS.put(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 1), farmland);
            MODELS.put(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7), new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier("minecraft:block/farmland_moist"))});


            var list = new ArrayList<BlockState>();
            for (int i = 2; i < 7; i++) {
                var state = Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, i);
                list.add(state);
                MODELS.put(state, farmland);
            }

            USABLE_STATES.put(BlockModelType.FARMLAND_BLOCK, list);
        }

        {
            var vines = new ArrayList<BlockState>();

            for (var block : new Block[]{Blocks.TWISTING_VINES, Blocks.WEEPING_VINES}) {
                var id = Registries.BLOCK.getId(block);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                vines.addAll(block.getStateManager().getStates());
                vines.remove(block.getDefaultState());
            }

            {
                var id = Registries.BLOCK.getId(Blocks.CAVE_VINES);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                var model2 = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath() + "_lit"))};
                for (var state : Blocks.CAVE_VINES.getStateManager().getStates()) {
                    var berries = state.get(CaveVines.BERRIES);
                    MODELS.put(state, berries ? model2 : model);
                    SPECIAL_REMAPS.put(state, Blocks.CAVE_VINES.getDefaultState().with(CaveVines.BERRIES, berries));
                }

                vines.addAll(Blocks.CAVE_VINES.getStateManager().getStates());
                vines.remove(Blocks.CAVE_VINES.getDefaultState());
                vines.remove(Blocks.CAVE_VINES.getDefaultState().with(CaveVines.BERRIES, true));
            }

            USABLE_STATES.put(BlockModelType.VINES_BLOCK, vines);
        }


        {
            var plant = new ArrayList<BlockState>();

            {
                var id = Registries.BLOCK.getId(Blocks.SUGAR_CANE);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : Blocks.SUGAR_CANE.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                plant.addAll(Blocks.SUGAR_CANE.getStateManager().getStates());
                plant.remove(Blocks.SUGAR_CANE.getDefaultState());

                USABLE_STATES.put(BlockModelType.BIOME_PLANT_BLOCK, plant);
            }
        }

        {
            var plant = new ArrayList<BlockState>();

            for (var block : new Block[]{Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING}) {
                var id = Registries.BLOCK.getId(block);

                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                plant.addAll(block.getStateManager().getStates());
                plant.remove(block.getDefaultState());
            }

            USABLE_STATES.put(BlockModelType.PLANT_BLOCK, plant);
        }
    }

    private static void generateDefault(BlockModelType type, Block... blocks) {
        generateDefault(type, (b) -> true, blocks);
    }

    private static void generateDefault(BlockModelType type, Predicate<BlockState> shouldInclude, Block... blocks) {
        var list = new ArrayList<BlockState>();

        for (var block : blocks) {
            var id = Registries.BLOCK.getId(block);
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
            for (var state : block.getStateManager().getStates()) {
                MODELS.put(state, model);
                if (shouldInclude.test(state)) {
                    list.add(state);
                }
            }

            if (block instanceof LeavesBlock) {
                list.remove(block.getDefaultState().with(LeavesBlock.PERSISTENT, true));
                list.remove(block.getDefaultState().with(LeavesBlock.PERSISTENT, true).with(LeavesBlock.WATERLOGGED, true));
            } else {
                list.remove(block.getDefaultState());
            }
        }

        USABLE_STATES.put(type, list);
    }
}
