package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.core.impl.PolymerImpl;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Predicate;

public class DefaultModelData {
    public static final Map<BlockModelType, List<BlockState>> USABLE_STATES = new EnumMap<>(BlockModelType.class);
    public static final Map<BlockState, BlockState> SPECIAL_REMAPS = new IdentityHashMap<>();
    public static final Map<BlockState, PolymerBlockModel[]> MODELS = new IdentityHashMap<>();

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
            var farmland = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of("minecraft:block/farmland"))};
            MODELS.put(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 1), farmland);
            MODELS.put(Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7), new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of("minecraft:block/farmland_moist"))});


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
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                vines.addAll(block.getStateManager().getStates());
                vines.remove(block.getDefaultState());
            }

            {
                var id = Registries.BLOCK.getId(Blocks.CAVE_VINES);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath()))};
                var model2 = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath() + "_lit"))};
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
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath()))};
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

            for (var block : new Block[]{Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING}) {
                var id = Registries.BLOCK.getId(block);

                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateManager().getStates()) {
                    MODELS.put(state, model);
                }

                plant.addAll(block.getStateManager().getStates());
                plant.remove(block.getDefaultState());
            }

            USABLE_STATES.put(BlockModelType.PLANT_BLOCK, plant);
        }

        {
            addDisarmedTripwire(false, BlockModelType.TRIPWIRE_BLOCK);
            addDisarmedTripwire(true, BlockModelType.TRIPWIRE_BLOCK_FLAT);

            addSlabs(SlabType.TOP, false, BlockModelType.TOP_SLAB);
            addSlabs(SlabType.TOP, true, BlockModelType.TOP_SLAB_WATERLOGGED);
            addSlabs(SlabType.BOTTOM, false, BlockModelType.BOTTOM_SLAB);
            addSlabs(SlabType.BOTTOM, true, BlockModelType.BOTTOM_SLAB_WATERLOGGED);
        }


        if (false && PolymerImpl.DEV_ENV) {
            PolymerImpl.LOGGER.info("===== Available States =====");
            for (var model : BlockModelType.values()) {
                PolymerImpl.LOGGER.info("{}: {}", model.name(), USABLE_STATES.get(model).size());

            }
        }
    }

    private static void addSlabs(SlabType slabType, boolean waterlogged, BlockModelType modelType) {
        ObjectArrayList<BlockState> list = new ObjectArrayList<>();

        addSlab(slabType, waterlogged, Blocks.OAK_SLAB, Blocks.PETRIFIED_OAK_SLAB, list);
        addSlab(slabType, waterlogged, Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB, list);
        addSlab(slabType, waterlogged, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, list);
        addSlab(slabType, waterlogged, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, list);
        addSlab(slabType, waterlogged, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB, list);

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }

    private static void addSlab(SlabType slabType, boolean waterlogged, Block to, Block from, ObjectArrayList<BlockState> list) {
        BlockState state = from.getDefaultState().with(SlabBlock.WATERLOGGED, waterlogged).with(SlabBlock.TYPE, slabType);
        list.add(state);
        DefaultModelData.SPECIAL_REMAPS.put(state, to.getStateWithProperties(state));
    }

    private static void addDisarmedTripwire(boolean attached, BlockModelType modelType) {
        ObjectArrayList<BlockState> list = new ObjectArrayList<>();
        // generate all permutations of north, south, east, west, powered
        {
            var base = Blocks.TRIPWIRE.getDefaultState().with(TripwireBlock.DISARMED, true);
            var booleans = new boolean[]{true, false};
            for (boolean north : booleans) {
                for (boolean south : booleans) {
                    for (boolean east : booleans) {
                        for (boolean west : booleans) {
                            for (boolean powered : booleans) {
                                BlockState state = base
                                        .with(TripwireBlock.ATTACHED, attached)
                                        .with(TripwireBlock.NORTH, north)
                                        .with(TripwireBlock.SOUTH, south)
                                        .with(TripwireBlock.EAST, east)
                                        .with(TripwireBlock.WEST, west)
                                        .with(TripwireBlock.POWERED, powered);
                                list.add(state);
                                DefaultModelData.SPECIAL_REMAPS.put(state, state.with(TripwireBlock.DISARMED, false).with(TripwireBlock.POWERED, false));
                            }
                        }
                    }
                }
            }
        }

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }

    private static void generateDefault(BlockModelType type, Block... blocks) {
        generateDefault(type, (b) -> true, blocks);
    }

    private static void generateDefault(BlockModelType type, Predicate<BlockState> shouldInclude, Block... blocks) {
        var list = new ArrayList<BlockState>();

        for (var block : blocks) {
            var id = Registries.BLOCK.getId(block);
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.of(id.getNamespace() + ":block/" + id.getPath()))};
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
