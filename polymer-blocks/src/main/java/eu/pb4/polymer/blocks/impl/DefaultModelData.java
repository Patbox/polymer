package eu.pb4.polymer.blocks.impl;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.core.impl.PolymerImpl;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

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

        {
            addTrapdoorDirection(Direction.NORTH, BlockHalf.TOP, false, BlockModelType.NORTH_TRAPDOOR);
            addTrapdoorDirection(Direction.EAST, BlockHalf.TOP, false, BlockModelType.EAST_TRAPDOOR);
            addTrapdoorDirection(Direction.SOUTH, BlockHalf.TOP, false, BlockModelType.SOUTH_TRAPDOOR);
            addTrapdoorDirection(Direction.WEST, BlockHalf.TOP, false, BlockModelType.WEST_TRAPDOOR);

            addTrapdoorDirection(Direction.NORTH, BlockHalf.TOP, true, BlockModelType.NORTH_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.EAST, BlockHalf.TOP, true, BlockModelType.EAST_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.SOUTH, BlockHalf.TOP, true, BlockModelType.SOUTH_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.WEST, BlockHalf.TOP, true, BlockModelType.WEST_TRAPDOOR_WATERLOGGED);

            addTrapdoorDirection(Direction.NORTH, BlockHalf.BOTTOM, false, BlockModelType.NORTH_TRAPDOOR);
            addTrapdoorDirection(Direction.EAST, BlockHalf.BOTTOM, false, BlockModelType.EAST_TRAPDOOR);
            addTrapdoorDirection(Direction.SOUTH, BlockHalf.BOTTOM, false, BlockModelType.SOUTH_TRAPDOOR);
            addTrapdoorDirection(Direction.WEST, BlockHalf.BOTTOM, false, BlockModelType.WEST_TRAPDOOR);

            addTrapdoorDirection(Direction.NORTH, BlockHalf.BOTTOM, true, BlockModelType.NORTH_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.EAST, BlockHalf.BOTTOM, true, BlockModelType.EAST_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.SOUTH, BlockHalf.BOTTOM, true, BlockModelType.SOUTH_TRAPDOOR_WATERLOGGED);
            addTrapdoorDirection(Direction.WEST, BlockHalf.BOTTOM, true, BlockModelType.WEST_TRAPDOOR_WATERLOGGED);

            addTrapdoorHalf(Direction.NORTH, BlockHalf.TOP, false, BlockModelType.TOP_TRAPDOOR);
            addTrapdoorHalf(Direction.EAST, BlockHalf.TOP, false, BlockModelType.TOP_TRAPDOOR);
            addTrapdoorHalf(Direction.SOUTH, BlockHalf.TOP, false, BlockModelType.TOP_TRAPDOOR);
            addTrapdoorHalf(Direction.WEST, BlockHalf.TOP, false, BlockModelType.TOP_TRAPDOOR);

            addTrapdoorHalf(Direction.NORTH, BlockHalf.TOP, true, BlockModelType.TOP_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.EAST, BlockHalf.TOP, true, BlockModelType.TOP_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.SOUTH, BlockHalf.TOP, true, BlockModelType.TOP_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.WEST, BlockHalf.TOP, true, BlockModelType.TOP_TRAPDOOR_WATERLOGGED);

            addTrapdoorHalf(Direction.NORTH, BlockHalf.BOTTOM, false, BlockModelType.BOTTOM_TRAPDOOR);
            addTrapdoorHalf(Direction.EAST, BlockHalf.BOTTOM, false, BlockModelType.BOTTOM_TRAPDOOR);
            addTrapdoorHalf(Direction.SOUTH, BlockHalf.BOTTOM, false, BlockModelType.BOTTOM_TRAPDOOR);
            addTrapdoorHalf(Direction.WEST, BlockHalf.BOTTOM, false, BlockModelType.BOTTOM_TRAPDOOR);

            addTrapdoorHalf(Direction.NORTH, BlockHalf.BOTTOM, true, BlockModelType.BOTTOM_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.EAST, BlockHalf.BOTTOM, true, BlockModelType.BOTTOM_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.SOUTH, BlockHalf.BOTTOM, true, BlockModelType.BOTTOM_TRAPDOOR_WATERLOGGED);
            addTrapdoorHalf(Direction.WEST, BlockHalf.BOTTOM, true, BlockModelType.BOTTOM_TRAPDOOR_WATERLOGGED);
        }

        {
            {
                List<BlockState> list = new ObjectArrayList<>();
                addDoor(Direction.NORTH, DoorHinge.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.NORTH, DoorHinge.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.NORTH, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.NORTH, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.WEST, DoorHinge.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.WEST, DoorHinge.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.EAST, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.EAST, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.NORTH_DOOR, list);
            }
            {
                List<BlockState> list = new ObjectArrayList<>();
                addDoor(Direction.EAST, DoorHinge.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.EAST, DoorHinge.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.EAST, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.EAST, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.NORTH, DoorHinge.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.NORTH, DoorHinge.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.SOUTH, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.SOUTH, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.EAST_DOOR, list);
            }
            {
                List<BlockState> list = new ObjectArrayList<>();
                addDoor(Direction.SOUTH, DoorHinge.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.SOUTH, DoorHinge.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.SOUTH, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.SOUTH, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.EAST, DoorHinge.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.EAST, DoorHinge.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.WEST, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.WEST, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SOUTH_DOOR, list);
            }
            {
                List<BlockState> list = new ObjectArrayList<>();
                addDoor(Direction.WEST, DoorHinge.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.WEST, DoorHinge.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.WEST, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.WEST, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.SOUTH, DoorHinge.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.SOUTH, DoorHinge.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.NORTH, DoorHinge.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.NORTH, DoorHinge.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.WEST_DOOR, list);
            }
        }

        {
            {
                List<BlockState> list = new ObjectArrayList<>();
                addSculkBlocks(false, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR_BLOCK, list);
            }
            {
                List<BlockState> list = new ObjectArrayList<>();
                addSculkBlocks(true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR_BLOCK_WATERLOGGED, list);
            }
        }

        if (false && PolymerImpl.DEV_ENV) {
            PolymerImpl.LOGGER.info("===== Available States =====");
            for (var model : BlockModelType.values()) {
                PolymerImpl.LOGGER.info("{}: {}", model.name(), USABLE_STATES.get(model).size());

            }
        }
    }

    private static void addSculkBlocks(boolean waterlogged, List<BlockState> list) {
        for (SculkSensorPhase phase : SculkSensorPhase.values()) {
            if (phase == SculkSensorPhase.ACTIVE) continue;
            for (int i = 1; i <= 15; i++) {
                BlockState defaultState = Blocks.SCULK_SENSOR.getDefaultState().with(SculkSensorBlock.SCULK_SENSOR_PHASE, phase).with(SculkSensorBlock.WATERLOGGED, waterlogged);
                BlockState from = defaultState.with(SculkSensorBlock.POWER, i);
                list.add(from);
                DefaultModelData.SPECIAL_REMAPS.put(from, defaultState);
            }
        }

        Direction[] facingDirs = new Direction[]{
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        };

        for (Direction direction : facingDirs) {
            for (SculkSensorPhase phase : SculkSensorPhase.values()) {
                if (phase == SculkSensorPhase.ACTIVE) continue;
                for (int i = 1; i <= 15; i++) {
                    BlockState defaultState = Blocks.CALIBRATED_SCULK_SENSOR.getDefaultState().with(SculkSensorBlock.SCULK_SENSOR_PHASE, phase).with(SculkSensorBlock.WATERLOGGED, waterlogged).with(CalibratedSculkSensorBlock.FACING, direction);
                    BlockState from = defaultState.with(SculkSensorBlock.POWER, i);
                    list.add(from);
                    DefaultModelData.SPECIAL_REMAPS.put(from, defaultState);
                }
            }
        }
    }

    private static void addDoor(Direction direction, DoorHinge doorHinge, DoubleBlockHalf doubleBlockHalf, boolean open, List<BlockState> list) {
        list.add(addSingleDoor(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSingleDoor(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSingleDoor(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSingleDoor(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));

        list.add(addSinglePoweredDoor(Blocks.ACACIA_DOOR, Blocks.ACACIA_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.BAMBOO_DOOR, Blocks.BAMBOO_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.BIRCH_DOOR, Blocks.BIRCH_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.CHERRY_DOOR, Blocks.CHERRY_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.CRIMSON_DOOR, Blocks.CRIMSON_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.JUNGLE_DOOR, Blocks.JUNGLE_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.MANGROVE_DOOR, Blocks.MANGROVE_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.OAK_DOOR, Blocks.OAK_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.SPRUCE_DOOR, Blocks.SPRUCE_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.WARPED_DOOR, Blocks.WARPED_DOOR, direction, doorHinge, doubleBlockHalf, open));

        list.add(addSinglePoweredDoor(Blocks.IRON_DOOR, Blocks.IRON_DOOR, direction, doorHinge, doubleBlockHalf, open));

        list.add(addSinglePoweredDoor(Blocks.WAXED_COPPER_DOOR, Blocks.WAXED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.WAXED_WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.WAXED_EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
        list.add(addSinglePoweredDoor(Blocks.WAXED_OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR, direction, doorHinge, doubleBlockHalf, open));
    }

    private static BlockState addSinglePoweredDoor(Block block, Block replacement, Direction facing, DoorHinge hinge, DoubleBlockHalf half, boolean open) {
        BlockState from = block.getDefaultState().with(DoorBlock.POWERED, true).with(DoorBlock.OPEN, open).with(DoorBlock.FACING, facing).with(DoorBlock.HALF, half).with(DoorBlock.HINGE, hinge);
        BlockState to = replacement.getStateWithProperties(from).with(DoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSingleDoor(Block block, Block replacement, Direction facing, DoorHinge hinge, DoubleBlockHalf half, boolean open) {
        BlockState from = block.getDefaultState().with(DoorBlock.POWERED, false).with(DoorBlock.OPEN, open).with(DoorBlock.FACING, facing).with(DoorBlock.HALF, half).with(DoorBlock.HINGE, hinge);
        BlockState to = replacement.getStateWithProperties(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static void addTrapdoorHalf(Direction facing, BlockHalf half, boolean waterlogged, BlockModelType modelType) {
        ObjectArrayList<BlockState> list = new ObjectArrayList<>();
        list.add(addSingleClosedTrapdoor(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleClosedTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleClosedTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleClosedTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.ACACIA_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.BAMBOO_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.BIRCH_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.CHERRY_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.CRIMSON_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.DARK_OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.JUNGLE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.MANGROVE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.SPRUCE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WARPED_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.IRON_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, facing, half, waterlogged));

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }

    private static void addTrapdoorDirection(Direction facing, BlockHalf half, boolean waterlogged, BlockModelType modelType) {
        ObjectArrayList<BlockState> list = new ObjectArrayList<>();

        list.add(addSingleOpenTrapdoor(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleOpenTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleOpenTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSingleOpenTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.ACACIA_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.BAMBOO_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.BIRCH_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.CHERRY_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.CRIMSON_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.DARK_OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.JUNGLE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.MANGROVE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.SPRUCE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WARPED_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.IRON_TRAPDOOR, facing, half, waterlogged));

        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredOpenTrapdoor(Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, facing, half, waterlogged));

        DefaultModelData.USABLE_STATES.put(modelType, list);
    }

    private static BlockState addSingleOpenTrapdoor(Block block, Block replacement, Direction facing, BlockHalf half, boolean waterlogged) {
        BlockState from = block.getDefaultState().with(TrapdoorBlock.OPEN, true).with(TrapdoorBlock.WATERLOGGED, waterlogged).with(TrapdoorBlock.FACING, facing).with(TrapdoorBlock.HALF, half);
        BlockState to = replacement.getStateWithProperties(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSingleClosedTrapdoor(Block block, Block replacement, Direction facing, BlockHalf half, boolean waterlogged) {
        BlockState from = block.getDefaultState().with(TrapdoorBlock.OPEN, false).with(TrapdoorBlock.WATERLOGGED, waterlogged).with(TrapdoorBlock.FACING, facing).with(TrapdoorBlock.HALF, half);
        BlockState to = replacement.getStateWithProperties(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSinglePoweredOpenTrapdoor(Block block, Direction facing, BlockHalf half, boolean waterlogged) {
        BlockState from = block.getDefaultState().with(TrapdoorBlock.OPEN, true).with(TrapdoorBlock.POWERED, true).with(TrapdoorBlock.WATERLOGGED, waterlogged).with(TrapdoorBlock.FACING, facing).with(TrapdoorBlock.HALF, half);
        BlockState to = from.with(TrapdoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSinglePoweredClosedTrapdoor(Block block, Direction facing, BlockHalf half, boolean waterlogged) {
        BlockState from = block.getDefaultState().with(TrapdoorBlock.OPEN, false).with(TrapdoorBlock.POWERED, true).with(TrapdoorBlock.WATERLOGGED, waterlogged).with(TrapdoorBlock.FACING, facing).with(TrapdoorBlock.HALF, half);
        BlockState to = from.with(TrapdoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
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
