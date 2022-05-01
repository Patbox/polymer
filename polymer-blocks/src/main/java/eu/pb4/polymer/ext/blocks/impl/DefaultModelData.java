package eu.pb4.polymer.ext.blocks.impl;

import eu.pb4.polymer.ext.blocks.api.BlockModelType;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import net.minecraft.block.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultModelData {
    public static final Map<BlockModelType, List<BlockState>> USABLE_STATES = new HashMap<>();
    public static final Map<BlockState, BlockState> SPECIAL_REMAPS = new HashMap<>();
    public static final Map<BlockState, PolymerBlockModel[]> MODELS = new HashMap<>();

    static {
        generateDefault(BlockModelType.FULL_BLOCK, Blocks.NOTE_BLOCK);
        generateDefault(BlockModelType.TRANSPARENT_BLOCK, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES);
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
                var id = Registry.BLOCK.getId(block);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                vines.addAll(block.getStateManager().getStates());
                vines.remove(block.getDefaultState());
            }

            {
                var id = Registry.BLOCK.getId(Blocks.CAVE_VINES);
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
                var id = Registry.BLOCK.getId(Blocks.SUGAR_CANE);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : Blocks.SUGAR_CANE.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                plant.addAll(Blocks.SUGAR_CANE.getStateManager().getStates());
                plant.remove(Blocks.SUGAR_CANE.getDefaultState());
            }

            for (var block : new Block[]{Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING}) {
                var id = Registry.BLOCK.getId(block);

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
        var list = new ArrayList<BlockState>();

        for (var block : blocks) {
            var id = Registry.BLOCK.getId(block);
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(new Identifier(id.getNamespace() + ":block/" + id.getPath()))};
            for (var state : block.getStateManager().getStates()) {
                MODELS.put(state, model);
            }

            list.addAll(block.getStateManager().getStates());
            list.remove(block.getDefaultState());
        }

        USABLE_STATES.put(type, list);
    }
}
