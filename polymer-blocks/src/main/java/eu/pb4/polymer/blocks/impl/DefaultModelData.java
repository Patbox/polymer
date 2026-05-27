package eu.pb4.polymer.blocks.impl;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.MultiPolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.core.impl.PolymerImpl;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SideChainPart;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import java.util.*;
import java.util.function.Predicate;

public class DefaultModelData {
    public static final Map<BlockModelType, List<BlockState>> USABLE_STATES = new EnumMap<>(BlockModelType.class);
    public static final Map<BlockState, BlockState> SPECIAL_REMAPS = new IdentityHashMap<>();
    public static final Map<BlockState, Either<PolymerBlockModel[], MultiPolymerBlockModel>> MODELS = new IdentityHashMap<>();

    private static final Predicate<BlockState> WATERLOGGED_PREDICATE = (state -> state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED));
    private static final Predicate<BlockState> NOT_WATERLOGGED_PREDICATE = (state -> !(state.getBlock() instanceof SimpleWaterloggedBlock && state.getValue(BlockStateProperties.WATERLOGGED)));

    static {
        var bools = new boolean[]{false, true};

        generateDefault(BlockModelType.FULL_BLOCK, Blocks.NOTE_BLOCK, Blocks.TARGET);
        {
            var list = USABLE_STATES.get(BlockModelType.FULL_BLOCK);

            for (var pair : List.of(Blocks.BEEHIVE, Blocks.BEE_NEST)) {
                for (var dir : Direction.Plane.HORIZONTAL) {
                    var base = pair.defaultBlockState().setValue(BeehiveBlock.FACING, dir);
                    for (int lvl = 1; lvl < 5; lvl++) {
                        var state = base.setValue(BeehiveBlock.HONEY_LEVEL, lvl);
                        list.add(state);
                        SPECIAL_REMAPS.put(state, base);
                    }
                }
            }

            for (var pair : List.of(Blocks.DISPENSER, Blocks.DROPPER)) {
                for (var dir : Direction.values()) {
                    var base = pair.defaultBlockState().setValue(DispenserBlock.FACING, dir);
                    var state = base.setValue(DispenserBlock.TRIGGERED, true);
                    list.add(state);
                    SPECIAL_REMAPS.put(state, base);
                }
            }

            for (var pair : List.of(Blocks.CREAKING_HEART)) {
                for (var dir : Direction.Axis.values()) {
                    for (var active : CreakingHeartState.values()) {
                        var base = pair.defaultBlockState().setValue(CreakingHeartBlock.AXIS, dir).setValue(CreakingHeartBlock.STATE, active);
                        var state = base.setValue(CreakingHeartBlock.NATURAL, true);
                        list.add(state);
                        SPECIAL_REMAPS.put(state, base);
                    }
                }
            }

            var cList = new ArrayList<>(List.of(
                    Pair.of(Blocks.INFESTED_STONE, Blocks.STONE),
                    Pair.of(Blocks.INFESTED_COBBLESTONE, Blocks.COBBLESTONE),
                    Pair.of(Blocks.INFESTED_STONE_BRICKS, Blocks.STONE_BRICKS),
                    Pair.of(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS),
                    Pair.of(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS),
                    Pair.of(Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS),
                    Pair.of(Blocks.INFESTED_DEEPSLATE, Blocks.CHISELED_DEEPSLATE)
            ));

            Blocks.COPPER_BLOCK.zipUnwaxedWaxed((a, b) -> cList.add(Pair.of(b, a)));

            for (var pair : cList) {
                for (var state : pair.getFirst().getStateDefinition().getPossibleStates()) {
                    list.add(state);
                    SPECIAL_REMAPS.put(state, pair.getSecond().withPropertiesOf(state));
                }
            }
        }
        generateDefault(BlockModelType.BIOME_COLORED_LEAVES, NOT_WATERLOGGED_PREDICATE, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        generateDefault(BlockModelType.BIOME_COLORED_LEAVES_WATERLOGGED, WATERLOGGED_PREDICATE, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        generateDefault(BlockModelType.LEAVES, NOT_WATERLOGGED_PREDICATE, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES);
        generateDefault(BlockModelType.LEAVES_WATERLOGGED, WATERLOGGED_PREDICATE, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES);
        generateDefault(BlockModelType.KELP, Blocks.KELP);
        generateDefault(BlockModelType.CACTUS, Blocks.CACTUS);

        {
            var farmland = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse("minecraft:block/farmland"))};
            MODELS.put(Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 1), Either.left(farmland));
            MODELS.put(Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7), Either.left(new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse("minecraft:block/farmland_moist"))}));


            var list = new ReferenceArrayList<BlockState>();
            for (int i = 2; i < 7; i++) {
                var state = Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, i);
                list.add(state);
                MODELS.put(state, Either.left(farmland));
            }

            USABLE_STATES.put(BlockModelType.FARMLAND, list);
        }

        {
            var vines = new ReferenceArrayList<BlockState>();

            for (var block : new Block[]{Blocks.TWISTING_VINES, Blocks.WEEPING_VINES}) {
                var id = BuiltInRegistries.BLOCK.getKey(block);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    MODELS.put(state, Either.left(model));
                }

                vines.addAll(block.getStateDefinition().getPossibleStates());
                vines.remove(block.defaultBlockState());
            }

            {
                var id = BuiltInRegistries.BLOCK.getKey(Blocks.CAVE_VINES);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath()))};
                var model2 = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath() + "_lit"))};
                for (var state : Blocks.CAVE_VINES.getStateDefinition().getPossibleStates()) {
                    var berries = state.getValue(CaveVines.BERRIES);
                    MODELS.put(state, Either.left(berries ? model2 : model));
                    SPECIAL_REMAPS.put(state, Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, berries));
                }

                vines.addAll(Blocks.CAVE_VINES.getStateDefinition().getPossibleStates());
                vines.remove(Blocks.CAVE_VINES.defaultBlockState());
                vines.remove(Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true));
            }

            USABLE_STATES.put(BlockModelType.VINES, vines);
        }


        {
            var plant = new ReferenceArrayList<BlockState>();

            {
                var id = BuiltInRegistries.BLOCK.getKey(Blocks.SUGAR_CANE);
                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : Blocks.SUGAR_CANE.getStateDefinition().getPossibleStates()) {
                    MODELS.put(state, Either.left(model));
                }

                plant.addAll(Blocks.SUGAR_CANE.getStateDefinition().getPossibleStates());
                plant.remove(Blocks.SUGAR_CANE.defaultBlockState());

                USABLE_STATES.put(BlockModelType.BIOME_PLANT, plant);
            }
        }

        {
            var plant = new ReferenceArrayList<BlockState>();

            for (var block : new Block[]{Blocks.OAK_SAPLING, Blocks.BIRCH_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.PALE_OAK_SAPLING}) {
                var id = BuiltInRegistries.BLOCK.getKey(block);

                var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath()))};
                for (var state : block.getStateDefinition().getPossibleStates()) {
                    MODELS.put(state, Either.left(model));
                }

                plant.addAll(block.getStateDefinition().getPossibleStates());
                plant.remove(block.defaultBlockState());
            }

            USABLE_STATES.put(BlockModelType.PLANT, plant);
        }

        {
            var states = new ReferenceArrayList<BlockState>();

            for (var block : new Block[]{Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE}) {
                var defaultState = block.defaultBlockState();
                var firstState = block.defaultBlockState().setValue(WeightedPressurePlateBlock.POWER, 1);
                for (int i = 2; i <= 15; i++) {
                    SPECIAL_REMAPS.put(defaultState.setValue(WeightedPressurePlateBlock.POWER, i), firstState);
                }

                states.addAll(block.getStateDefinition().getPossibleStates());
                states.remove(defaultState);
                states.remove(firstState);
            }

            USABLE_STATES.put(BlockModelType.PRESSURE_PLATE_ACTIVE, states);
        }

        {
            var r = new ReferenceArrayList<BlockState>();
            var w = new ReferenceArrayList<BlockState>();

            for (var block : new Block[]{Blocks.SOUL_CAMPFIRE}) {
                var state = block.defaultBlockState().setValue(CampfireBlock.LIT, false);
                for (var dir : Direction.Plane.HORIZONTAL) {
                    for (var signal : bools) {
                        for (var waterlogged : bools) {
                            state = state.setValue(CampfireBlock.FACING, dir).setValue(CampfireBlock.SIGNAL_FIRE, signal).setValue(CampfireBlock.WATERLOGGED, waterlogged);
                            SPECIAL_REMAPS.put(state, Blocks.CAMPFIRE.withPropertiesOf(state));
                            (waterlogged ? w : r).add(state);
                        }
                    }
                }
            }

            USABLE_STATES.put(BlockModelType.CAMPFIRE, r);
            USABLE_STATES.put(BlockModelType.CAMPFIRE_WATERLOGGED, w);
        }

        {
            var r = new ReferenceArrayList<BlockState>();

            for (var block : new Block[]{ Blocks.PLAYER_HEAD }) {
                var state = block.defaultBlockState().setValue(SkullBlock.POWERED, true);
                for (int i = 0; i <= RotationSegment.getMaxSegmentIndex(); i++) {
                    state = state.setValue(SkullBlock.ROTATION, i);
                    SPECIAL_REMAPS.put(state, state.setValue(SkullBlock.POWERED, false));
                    r.add(state);
                }
            }

            USABLE_STATES.put(BlockModelType.HEAD, r);
        }

        {
            record Bound(BlockModelType type, Direction direction, Half blockHalf, StairsShape shape, boolean waterlogged, ReferenceArrayList<BlockState> list) { }

            var bounds = new ArrayList<Bound>();

            for (var dir : Direction.Plane.HORIZONTAL) {
                for (var half : Half.values()) {
                    for (var shape : StairsShape.values()) {
                        for (var waterlogged : bools) {
                            var b = new Bound(BlockModelType.getStairs(dir, half, shape, waterlogged), dir, half, shape, waterlogged, new ReferenceArrayList<>());
                            bounds.add(b);
                            USABLE_STATES.put(b.type, b.list);
                        }
                    }
                }
            }


            Blocks.CUT_COPPER_STAIRS.zipUnwaxedWaxed((b, a) -> {
                for (var bound : bounds) {
                    var state = a.defaultBlockState()
                            .setValue(StairBlock.FACING, bound.direction)
                            .setValue(StairBlock.HALF, bound.blockHalf)
                            .setValue(StairBlock.SHAPE, bound.shape)
                            .setValue(StairBlock.WATERLOGGED, bound.waterlogged());
                    SPECIAL_REMAPS.put(state, b.withPropertiesOf(state));
                    bound.list.add(state);
                }
            });
        }

        {
            record Bound(BlockModelType type, Direction direction, boolean waterlogged, ReferenceArrayList<BlockState> list) {}

            var bounds = new ArrayList<Bound>();

            for (var dir : Direction.Plane.HORIZONTAL) {
                bounds.add(new Bound(BlockModelType.getShelf(dir, false), dir, false, new ReferenceArrayList<>()));
                bounds.add(new Bound(BlockModelType.getShelf(dir, true), dir, true, new ReferenceArrayList<>()));
            }

            for (var block : List.of(
                    Blocks.OAK_SHELF, 
                    Blocks.SPRUCE_SHELF, 
                    Blocks.JUNGLE_SHELF, 
                    Blocks.ACACIA_SHELF, 
                    Blocks.DARK_OAK_SHELF, 
                    Blocks.MANGROVE_SHELF,
                    Blocks.BAMBOO_SHELF,
                    Blocks.PALE_OAK_SHELF,
                    Blocks.CHERRY_SHELF,
                    Blocks.WARPED_SHELF
            )) {
                for (var chain : SideChainPart.values()) {
                    if (chain == SideChainPart.UNCONNECTED) {
                        continue;
                    }
                    for (var bound : bounds) {
                        var state = block.defaultBlockState().setValue(ShelfBlock.SIDE_CHAIN_PART, chain).setValue(ShelfBlock.FACING, bound.direction())
                                .setValue(ShelfBlock.WATERLOGGED, bound.waterlogged()).setValue(ShelfBlock.POWERED, false);
                        SPECIAL_REMAPS.put(state, state.setValue(ShelfBlock.SIDE_CHAIN_PART, SideChainPart.UNCONNECTED));
                        bound.list.add(state);
                    }
                }
            }

            for (var b : bounds) {
                USABLE_STATES.put(b.type(), b.list());
            }
        }


        {
            var x = new ReferenceArrayList<BlockState>();
            var y = new ReferenceArrayList<BlockState>();
            var z = new ReferenceArrayList<BlockState>();
            var xw = new ReferenceArrayList<BlockState>();
            var yw = new ReferenceArrayList<BlockState>();
            var zw = new ReferenceArrayList<BlockState>();


            Blocks.LIGHTNING_ROD.zipUnwaxedWaxed((b, a) -> {
                for (var powered : bools) {
                    for (var waterlogged : bools) {
                        for (var dir : Direction.values()) {
                            var list = switch (dir.getAxis()) {
                                case X -> waterlogged ? xw : x;
                                case Y -> waterlogged ? yw : y;
                                case Z -> waterlogged ? zw : z;
                            };

                            var state = a.defaultBlockState()
                                    .setValue(LightningRodBlock.POWERED, powered)
                                    .setValue(LightningRodBlock.WATERLOGGED, waterlogged)
                                    .setValue(LightningRodBlock.FACING, dir);

                            var base = powered ? Blocks.LIGHTNING_ROD.weathering().unaffected() : b;

                            list.add(state);
                            SPECIAL_REMAPS.put(state, base.withPropertiesOf(state));

                            if (powered && b != Blocks.LIGHTNING_ROD.weathering().unaffected()) {
                                state = b.withPropertiesOf(state);
                                list.add(state);
                                SPECIAL_REMAPS.put(state, base.withPropertiesOf(state));
                            }
                        }
                    }
                }
            });

            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_X, x);
            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_Y, y);
            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_Z, z);
            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_X_WATERLOGGED, xw);
            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_Y_WATERLOGGED, yw);
            USABLE_STATES.put(BlockModelType.LIGHTNING_ROD_Z_WATERLOGGED, zw);
        }

        {
            var x = new ReferenceArrayList<BlockState>();
            var y = new ReferenceArrayList<BlockState>();
            var z = new ReferenceArrayList<BlockState>();
            var xw = new ReferenceArrayList<BlockState>();
            var yw = new ReferenceArrayList<BlockState>();
            var zw = new ReferenceArrayList<BlockState>();

            Blocks.COPPER_CHAIN.zipUnwaxedWaxed((b, a) -> {
                for (var waterlogged : bools) {
                    for (var dir : Direction.Axis.values()) {
                        var list = switch (dir) {
                            case X -> waterlogged ? xw : x;
                            case Y -> waterlogged ? yw : y;
                            case Z -> waterlogged ? zw : z;
                        };

                        var state = b.defaultBlockState()
                                .setValue(ChainBlock.WATERLOGGED, waterlogged)
                                .setValue(ChainBlock.AXIS, dir);

                        list.add(state);
                        SPECIAL_REMAPS.put(state, a.withPropertiesOf(state));
                    }
                }
            });

            USABLE_STATES.put(BlockModelType.CHAIN_X, x);
            USABLE_STATES.put(BlockModelType.CHAIN_Y, y);
            USABLE_STATES.put(BlockModelType.CHAIN_Z, z);
            USABLE_STATES.put(BlockModelType.CHAIN_X_WATERLOGGED, xw);
            USABLE_STATES.put(BlockModelType.CHAIN_Y_WATERLOGGED, yw);
            USABLE_STATES.put(BlockModelType.CHAIN_Z_WATERLOGGED, zw);
        }

        {
            record Bound(BlockModelType type, List<BooleanProperty> properties, ReferenceArrayList<BlockState> list) {}
            var bounds = new ArrayList<Bound>();

            var properties = new BooleanProperty[] {
                    IronBarsBlock.WATERLOGGED,
                    IronBarsBlock.NORTH,
                    IronBarsBlock.SOUTH,
                    IronBarsBlock.WEST,
                    IronBarsBlock.EAST,
            };

            for (var i = 0; i < 32; i++) {
                var b = new Bound(BlockModelType.values()[BlockModelType.BARS_CENTER.ordinal() + i], new ArrayList<>(), new ReferenceArrayList<>());
                USABLE_STATES.put(b.type(), b.list());

                for (int a = 0; a < properties.length; a++) {
                    if (((i >> a) & 1) == 1) {
                        b.properties.add(properties[a]);
                    }
                }

                bounds.add(b);
            }

            Blocks.COPPER_BARS.zipUnwaxedWaxed((unwaxed, waxed) -> {
                for (var b : bounds) {
                    var state = waxed.defaultBlockState();
                    for (var p : b.properties) {
                        state = state.setValue(p, true);
                    }
                    b.list.add(state);
                    SPECIAL_REMAPS.put(state, unwaxed.withPropertiesOf(state));
                }
            });
        }

        {
            var r = new ReferenceArrayList<BlockState>();
            var h = new ReferenceArrayList<BlockState>();
            var rw = new ReferenceArrayList<BlockState>();
            var hw = new ReferenceArrayList<BlockState>();

            Blocks.COPPER_LANTERN.zipUnwaxedWaxed((unwaxed, waxed) -> {
                for (var hanging : bools) {
                    for (var waterlogged : bools) {
                        var list = hanging ? (waterlogged ? hw : h) : (waterlogged ? rw : r);

                        var state = waxed.defaultBlockState()
                                .setValue(LanternBlock.WATERLOGGED, waterlogged)
                                .setValue(LanternBlock.HANGING, hanging);

                        list.add(state);
                        SPECIAL_REMAPS.put(state, unwaxed.withPropertiesOf(state));
                    }
                }
            });

            USABLE_STATES.put(BlockModelType.LANTERN, r);
            USABLE_STATES.put(BlockModelType.LANTERN_HANGING, h);
            USABLE_STATES.put(BlockModelType.LANTERN_WATERLOGGED, rw);
            USABLE_STATES.put(BlockModelType.LANTERN_HANGING_WATERLOGGED, hw);
        }

        {
            addDisarmedTripwire(false, BlockModelType.TRIPWIRE);
            addDisarmedTripwire(true, BlockModelType.TRIPWIRE_FLAT);

            addSlabs(SlabType.TOP, false, BlockModelType.SLAB_TOP);
            addSlabs(SlabType.TOP, true, BlockModelType.SLAB_TOP_WATERLOGGED);
            addSlabs(SlabType.BOTTOM, false, BlockModelType.SLAB_BOTTOM);
            addSlabs(SlabType.BOTTOM, true, BlockModelType.SLAB_BOTTOM_WATERLOGGED);

            var fullSlabs = new ArrayList<>(List.of(
                    Pair.of(Blocks.RESIN_BRICK_SLAB, Blocks.RESIN_BRICKS),
                    Pair.of(Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE),
                    Pair.of(Blocks.PRISMARINE_BRICK_SLAB, Blocks.PRISMARINE_BRICKS),
                    Pair.of(Blocks.DARK_PRISMARINE_SLAB, Blocks.DARK_PRISMARINE),
                    Pair.of(Blocks.OAK_SLAB, Blocks.OAK_PLANKS),
                    Pair.of(Blocks.SPRUCE_SLAB, Blocks.SPRUCE_PLANKS),
                    Pair.of(Blocks.BIRCH_SLAB, Blocks.BIRCH_PLANKS),
                    Pair.of(Blocks.JUNGLE_SLAB, Blocks.JUNGLE_PLANKS),
                    Pair.of(Blocks.ACACIA_SLAB, Blocks.ACACIA_PLANKS),
                    Pair.of(Blocks.CHERRY_SLAB, Blocks.CHERRY_PLANKS),
                    Pair.of(Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_PLANKS),
                    Pair.of(Blocks.PALE_OAK_SLAB, Blocks.PALE_OAK_PLANKS),
                    Pair.of(Blocks.MANGROVE_SLAB, Blocks.MANGROVE_PLANKS),
                    Pair.of(Blocks.BAMBOO_SLAB, Blocks.BAMBOO_PLANKS),
                    Pair.of(Blocks.BAMBOO_MOSAIC_SLAB, Blocks.BAMBOO_MOSAIC),
                    Pair.of(Blocks.STONE_SLAB, Blocks.STONE),
                    Pair.of(Blocks.SANDSTONE_SLAB, Blocks.SANDSTONE),
                    Pair.of(Blocks.CUT_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE),
                    Pair.of(Blocks.PETRIFIED_OAK_SLAB, Blocks.OAK_PLANKS),
                    Pair.of(Blocks.COBBLESTONE_SLAB, Blocks.COBBLESTONE),
                    Pair.of(Blocks.BRICK_SLAB, Blocks.BRICKS),
                    Pair.of(Blocks.STONE_BRICK_SLAB, Blocks.STONE_BRICKS),
                    Pair.of(Blocks.MUD_BRICK_SLAB, Blocks.MUD_BRICKS),
                    Pair.of(Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS),
                    Pair.of(Blocks.QUARTZ_SLAB, Blocks.QUARTZ_BLOCK),
                    Pair.of(Blocks.RED_SANDSTONE_SLAB, Blocks.RED_SANDSTONE),
                    Pair.of(Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE),
                    Pair.of(Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK),
                    Pair.of(Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_GRANITE),
                    Pair.of(Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE),
                    Pair.of(Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICKS),
                    Pair.of(Blocks.POLISHED_DIORITE_SLAB, Blocks.POLISHED_DIORITE),
                    Pair.of(Blocks.MOSSY_COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE),
                    Pair.of(Blocks.END_STONE_BRICK_SLAB, Blocks.END_STONE_BRICKS),
                    Pair.of(Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE),
                    Pair.of(Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ),
                    Pair.of(Blocks.GRANITE_SLAB, Blocks.GRANITE),
                    Pair.of(Blocks.ANDESITE_SLAB, Blocks.ANDESITE),
                    Pair.of(Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS),
                    Pair.of(Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_ANDESITE),
                    Pair.of(Blocks.DIORITE_SLAB, Blocks.DIORITE),
                    Pair.of(Blocks.CRIMSON_SLAB, Blocks.CRIMSON_PLANKS),
                    Pair.of(Blocks.WARPED_SLAB, Blocks.WARPED_PLANKS),
                    Pair.of(Blocks.BLACKSTONE_SLAB, Blocks.BLACKSTONE),
                    Pair.of(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.POLISHED_BLACKSTONE_BRICKS),
                    Pair.of(Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE),
                    Pair.of(Blocks.TUFF_SLAB, Blocks.TUFF),
                    Pair.of(Blocks.POLISHED_TUFF_SLAB, Blocks.POLISHED_TUFF),
                    Pair.of(Blocks.TUFF_BRICK_SLAB, Blocks.TUFF_BRICKS),
                    Pair.of(Blocks.CUT_COPPER_SLAB.weathering().oxidized(), Blocks.CUT_COPPER.weathering().oxidized()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.weathering().weathered(), Blocks.CUT_COPPER.weathering().weathered()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.weathering().exposed(), Blocks.CUT_COPPER.weathering().exposed()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.weathering().unaffected(), Blocks.CUT_COPPER.weathering().unaffected()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.waxed().oxidized(), Blocks.CUT_COPPER.weathering().oxidized()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.waxed().weathered(), Blocks.CUT_COPPER.weathering().weathered()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.waxed().exposed(), Blocks.CUT_COPPER.weathering().exposed()),
                    Pair.of(Blocks.CUT_COPPER_SLAB.waxed().unaffected(), Blocks.CUT_COPPER.weathering().unaffected()),
                    Pair.of(Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE),
                    Pair.of(Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.POLISHED_DEEPSLATE),
                    Pair.of(Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES),
                    Pair.of(Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS)
            ));

            var fullRefs = USABLE_STATES.get(BlockModelType.FULL_BLOCK);
            for (var pair : fullSlabs) {
                addSlab(SlabType.DOUBLE, false, pair.getSecond(), pair.getFirst(), fullRefs);
            }

            for (var pair : fullSlabs) {
                addSlab(SlabType.DOUBLE, true, pair.getSecond(), pair.getFirst(), fullRefs);
            }
        }

        {
            addTrapdoorDirection(Direction.NORTH, Half.TOP, false, BlockModelType.TRAPDOOR_NORTH);
            addTrapdoorDirection(Direction.EAST, Half.TOP, false, BlockModelType.TRAPDOOR_EAST);
            addTrapdoorDirection(Direction.SOUTH, Half.TOP, false, BlockModelType.TRAPDOOR_SOUTH);
            addTrapdoorDirection(Direction.WEST, Half.TOP, false, BlockModelType.TRAPDOOR_WEST);

            addTrapdoorDirection(Direction.NORTH, Half.TOP, true, BlockModelType.TRAPDOOR_NORTH_WATERLOGGED);
            addTrapdoorDirection(Direction.EAST, Half.TOP, true, BlockModelType.TRAPDOOR_EAST_WATERLOGGED);
            addTrapdoorDirection(Direction.SOUTH, Half.TOP, true, BlockModelType.TRAPDOOR_SOUTH_WATERLOGGED);
            addTrapdoorDirection(Direction.WEST, Half.TOP, true, BlockModelType.TRAPDOOR_WEST_WATERLOGGED);

            addTrapdoorDirection(Direction.NORTH, Half.BOTTOM, false, BlockModelType.TRAPDOOR_NORTH);
            addTrapdoorDirection(Direction.EAST, Half.BOTTOM, false, BlockModelType.TRAPDOOR_EAST);
            addTrapdoorDirection(Direction.SOUTH, Half.BOTTOM, false, BlockModelType.TRAPDOOR_SOUTH);
            addTrapdoorDirection(Direction.WEST, Half.BOTTOM, false, BlockModelType.TRAPDOOR_WEST);

            addTrapdoorDirection(Direction.NORTH, Half.BOTTOM, true, BlockModelType.TRAPDOOR_NORTH_WATERLOGGED);
            addTrapdoorDirection(Direction.EAST, Half.BOTTOM, true, BlockModelType.TRAPDOOR_EAST_WATERLOGGED);
            addTrapdoorDirection(Direction.SOUTH, Half.BOTTOM, true, BlockModelType.TRAPDOOR_SOUTH_WATERLOGGED);
            addTrapdoorDirection(Direction.WEST, Half.BOTTOM, true, BlockModelType.TRAPDOOR_WEST_WATERLOGGED);

            addTrapdoorHalf(Direction.NORTH, Half.TOP, false, BlockModelType.TRAPDOOR_TOP);
            addTrapdoorHalf(Direction.EAST, Half.TOP, false, BlockModelType.TRAPDOOR_TOP);
            addTrapdoorHalf(Direction.SOUTH, Half.TOP, false, BlockModelType.TRAPDOOR_TOP);
            addTrapdoorHalf(Direction.WEST, Half.TOP, false, BlockModelType.TRAPDOOR_TOP);

            addTrapdoorHalf(Direction.NORTH, Half.TOP, true, BlockModelType.TRAPDOOR_TOP_WATERLOGGED);
            addTrapdoorHalf(Direction.EAST, Half.TOP, true, BlockModelType.TRAPDOOR_TOP_WATERLOGGED);
            addTrapdoorHalf(Direction.SOUTH, Half.TOP, true, BlockModelType.TRAPDOOR_TOP_WATERLOGGED);
            addTrapdoorHalf(Direction.WEST, Half.TOP, true, BlockModelType.TRAPDOOR_TOP_WATERLOGGED);

            addTrapdoorHalf(Direction.NORTH, Half.BOTTOM, false, BlockModelType.TRAPDOOR_BOTTOM);
            addTrapdoorHalf(Direction.EAST, Half.BOTTOM, false, BlockModelType.TRAPDOOR_BOTTOM);
            addTrapdoorHalf(Direction.SOUTH, Half.BOTTOM, false, BlockModelType.TRAPDOOR_BOTTOM);
            addTrapdoorHalf(Direction.WEST, Half.BOTTOM, false, BlockModelType.TRAPDOOR_BOTTOM);

            addTrapdoorHalf(Direction.NORTH, Half.BOTTOM, true, BlockModelType.TRAPDOOR_BOTTOM_WATERLOGGED);
            addTrapdoorHalf(Direction.EAST, Half.BOTTOM, true, BlockModelType.TRAPDOOR_BOTTOM_WATERLOGGED);
            addTrapdoorHalf(Direction.SOUTH, Half.BOTTOM, true, BlockModelType.TRAPDOOR_BOTTOM_WATERLOGGED);
            addTrapdoorHalf(Direction.WEST, Half.BOTTOM, true, BlockModelType.TRAPDOOR_BOTTOM_WATERLOGGED);
        }

        {
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addDoor(Direction.NORTH, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.NORTH, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.NORTH, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.NORTH, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.WEST, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.WEST, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.EAST, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.EAST, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.DOOR_NORTH, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addDoor(Direction.EAST, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.EAST, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.EAST, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.EAST, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.NORTH, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.NORTH, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.SOUTH, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.SOUTH, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.DOOR_EAST, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addDoor(Direction.SOUTH, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.SOUTH, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.SOUTH, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.SOUTH, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.EAST, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.EAST, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.WEST, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.WEST, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.DOOR_SOUTH, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addDoor(Direction.WEST, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.WEST, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.WEST, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, false, list);
                addDoor(Direction.WEST, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, false, list);
                addDoor(Direction.SOUTH, DoorHingeSide.LEFT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.SOUTH, DoorHingeSide.LEFT, DoubleBlockHalf.LOWER, true, list);
                addDoor(Direction.NORTH, DoorHingeSide.RIGHT, DoubleBlockHalf.UPPER, true, list);
                addDoor(Direction.NORTH, DoorHingeSide.RIGHT, DoubleBlockHalf.LOWER, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.DOOR_WEST, list);
            }
        }

        {
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addSculkBlocks(false, false, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addSculkBlocks(true, false, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR_WATERLOGGED, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addSculkBlocks(false, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR_ACTIVE, list);
            }
            {
                List<BlockState> list = new ReferenceArrayList<>();
                addSculkBlocks(true, true, list);
                DefaultModelData.USABLE_STATES.put(BlockModelType.SCULK_SENSOR_ACTIVE_WATERLOGGED, list);
            }
        }

        {
            addScaffolding(false, false, BlockModelType.SCAFFOLDING_TOP);
            addScaffolding(true, false, BlockModelType.SCAFFOLDING_BOTTOM);
            addScaffolding(false, true, BlockModelType.SCAFFOLDING_TOP_WATERLOGGED);
            addScaffolding(true, true, BlockModelType.SCAFFOLDING_BOTTOM_WATERLOGGED);
        }
        {
            addFenceGates(Blocks.ACACIA_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE, Blocks.BIRCH_FENCE_GATE,
                    Blocks.CHERRY_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
                    Blocks.JUNGLE_FENCE_GATE, Blocks.MANGROVE_FENCE_GATE, Blocks.OAK_FENCE_GATE,
                    Blocks.PALE_OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.WARPED_FENCE_GATE);
        }

        if (false) {
            PolymerImpl.LOGGER.info("===== Available States =====");
            for (var model : BlockModelType.values()) {
                PolymerImpl.LOGGER.info("{}: {}", model.name(), USABLE_STATES.get(model).size());

            }
        }
    }

    private static void addSculkBlocks(boolean waterlogged, boolean active, List<BlockState> list) {
        for (var phase : SculkSensorPhase.values()) {
            if ((phase == SculkSensorPhase.ACTIVE) != active) continue;
            for (int i = 1; i <= 15; i++) {
                var defaultState = Blocks.SCULK_SENSOR.defaultBlockState().setValue(SculkSensorBlock.PHASE, phase).setValue(SculkSensorBlock.WATERLOGGED, waterlogged);
                var from = defaultState.setValue(SculkSensorBlock.POWER, i);
                list.add(from);
                DefaultModelData.SPECIAL_REMAPS.put(from, defaultState);
            }
        }

        var facingDirs = new Direction[]{
                Direction.NORTH,
                Direction.EAST,
                Direction.SOUTH,
                Direction.WEST
        };

        for (var direction : facingDirs) {
            for (var phase : SculkSensorPhase.values()) {
                if ((phase == SculkSensorPhase.ACTIVE) != active) continue;
                for (int i = 1; i <= 15; i++) {
                    var defaultState = Blocks.CALIBRATED_SCULK_SENSOR.defaultBlockState().setValue(SculkSensorBlock.PHASE, phase).setValue(SculkSensorBlock.WATERLOGGED, waterlogged).setValue(CalibratedSculkSensorBlock.FACING, direction);
                    var from = defaultState.setValue(SculkSensorBlock.POWER, i);
                    list.add(from);
                    DefaultModelData.SPECIAL_REMAPS.put(from, defaultState);
                }
            }
        }
    }

    private static void addDoor(Direction direction, DoorHingeSide doorHinge, DoubleBlockHalf doubleBlockHalf, boolean open, List<BlockState> list) {
        Blocks.COPPER_DOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSingleDoor(unwaxed, waxed, direction, doorHinge, doubleBlockHalf, open));
        });

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
        list.add(addSinglePoweredDoor(Blocks.PALE_OAK_DOOR, Blocks.PALE_OAK_DOOR, direction, doorHinge, doubleBlockHalf, open));

        Blocks.COPPER_DOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSinglePoweredDoor(waxed, unwaxed, direction, doorHinge, doubleBlockHalf, open));
        });

        list.add(addSinglePoweredDoor(Blocks.IRON_DOOR, Blocks.IRON_DOOR, direction, doorHinge, doubleBlockHalf, open));
    }

    private static BlockState addSinglePoweredDoor(Block block, Block replacement, Direction facing, DoorHingeSide hinge, DoubleBlockHalf half, boolean open) {
        BlockState from = block.defaultBlockState().setValue(DoorBlock.POWERED, true).setValue(DoorBlock.OPEN, open).setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, half).setValue(DoorBlock.HINGE, hinge);
        BlockState to = replacement.withPropertiesOf(from).setValue(DoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSingleDoor(Block block, Block replacement, Direction facing, DoorHingeSide hinge, DoubleBlockHalf half, boolean open) {
        BlockState from = block.defaultBlockState().setValue(DoorBlock.POWERED, false).setValue(DoorBlock.OPEN, open).setValue(DoorBlock.FACING, facing).setValue(DoorBlock.HALF, half).setValue(DoorBlock.HINGE, hinge);
        BlockState to = replacement.withPropertiesOf(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static void addTrapdoorHalf(Direction facing, Half half, boolean waterlogged, BlockModelType modelType) {
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());

        Blocks.COPPER_TRAPDOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSingleClosedTrapdoor(unwaxed, waxed, facing, half, waterlogged));
        });

        list.add(addSinglePoweredClosedTrapdoor(Blocks.ACACIA_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.BAMBOO_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.BIRCH_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.CHERRY_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.CRIMSON_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.DARK_OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.JUNGLE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.MANGROVE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.OAK_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.SPRUCE_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.WARPED_TRAPDOOR, facing, half, waterlogged));
        list.add(addSinglePoweredClosedTrapdoor(Blocks.PALE_OAK_TRAPDOOR, facing, half, waterlogged));


        Blocks.COPPER_TRAPDOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSinglePoweredClosedTrapdoor(waxed, facing, half, waterlogged));
        });

        list.add(addSinglePoweredClosedTrapdoor(Blocks.IRON_TRAPDOOR, facing, half, waterlogged));
    }

    private static void addTrapdoorDirection(Direction facing, Half half, boolean waterlogged, BlockModelType modelType) {
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());

        Blocks.COPPER_TRAPDOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSingleOpenTrapdoor(unwaxed, waxed, facing, half, waterlogged));
        });

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
        list.add(addSinglePoweredOpenTrapdoor(Blocks.PALE_OAK_TRAPDOOR, facing, half, waterlogged));

        Blocks.COPPER_TRAPDOOR.zipUnwaxedWaxed((unwaxed, waxed) -> {
            list.add(addSinglePoweredOpenTrapdoor(waxed, facing, half, waterlogged));
        });

        list.add(addSinglePoweredOpenTrapdoor(Blocks.IRON_TRAPDOOR, facing, half, waterlogged));
    }

    private static BlockState addSingleOpenTrapdoor(Block block, Block replacement, Direction facing, Half half, boolean waterlogged) {
        BlockState from = block.defaultBlockState().setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.WATERLOGGED, waterlogged).setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half);
        BlockState to = replacement.withPropertiesOf(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSingleClosedTrapdoor(Block block, Block replacement, Direction facing, Half half, boolean waterlogged) {
        BlockState from = block.defaultBlockState().setValue(TrapDoorBlock.OPEN, false).setValue(TrapDoorBlock.WATERLOGGED, waterlogged).setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half);
        BlockState to = replacement.withPropertiesOf(from);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSinglePoweredOpenTrapdoor(Block block, Direction facing, Half half, boolean waterlogged) {
        BlockState from = block.defaultBlockState().setValue(TrapDoorBlock.OPEN, true).setValue(TrapDoorBlock.POWERED, true).setValue(TrapDoorBlock.WATERLOGGED, waterlogged).setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half);
        BlockState to = from.setValue(TrapDoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static BlockState addSinglePoweredClosedTrapdoor(Block block, Direction facing, Half half, boolean waterlogged) {
        BlockState from = block.defaultBlockState().setValue(TrapDoorBlock.OPEN, false).setValue(TrapDoorBlock.POWERED, true).setValue(TrapDoorBlock.WATERLOGGED, waterlogged).setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.HALF, half);
        BlockState to = from.setValue(TrapDoorBlock.POWERED, false);
        DefaultModelData.SPECIAL_REMAPS.put(from, to);
        return from;
    }

    private static void addSlabs(SlabType slabType, boolean waterlogged, BlockModelType modelType) {
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());

        Blocks.CUT_COPPER_SLAB.zipUnwaxedWaxed((unwaxed, waxed) -> {
            addSlab(slabType, waterlogged, unwaxed, waxed, list);
        });

        addSlab(slabType, waterlogged, Blocks.OAK_SLAB, Blocks.PETRIFIED_OAK_SLAB, list);
    }

    private static void addSlab(SlabType slabType, boolean waterlogged, Block to, Block from, List<BlockState> list) {
        BlockState state = from.defaultBlockState().setValue(SlabBlock.WATERLOGGED, waterlogged).setValue(SlabBlock.TYPE, slabType);
        list.add(state);
        DefaultModelData.SPECIAL_REMAPS.put(state, to.withPropertiesOf(state));
    }

    private static void addDisarmedTripwire(boolean attached, BlockModelType modelType) {
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());
        // generate all permutations of north, south, east, west, powered
        {
            var base = Blocks.TRIPWIRE.defaultBlockState().setValue(TripWireBlock.DISARMED, true);
            var booleans = new boolean[]{true, false};
            for (boolean north : booleans) {
                for (boolean south : booleans) {
                    for (boolean east : booleans) {
                        for (boolean west : booleans) {
                            for (boolean powered : booleans) {
                                BlockState state = base
                                        .setValue(TripWireBlock.ATTACHED, attached)
                                        .setValue(TripWireBlock.NORTH, north)
                                        .setValue(TripWireBlock.SOUTH, south)
                                        .setValue(TripWireBlock.EAST, east)
                                        .setValue(TripWireBlock.WEST, west)
                                        .setValue(TripWireBlock.POWERED, powered);
                                list.add(state);
                                DefaultModelData.SPECIAL_REMAPS.put(state, state.setValue(TripWireBlock.DISARMED, false).setValue(TripWireBlock.POWERED, false));
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addScaffolding(boolean bottom, boolean waterlogged, BlockModelType modelType) {

        var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse("minecraft:block/scaffolding_" + (bottom ? "unstable" : "stable")))};
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());

        for (int i = 0; i <= 7; i++) {
            var state = Blocks.SCAFFOLDING.defaultBlockState()
                    .setValue(ScaffoldingBlock.BOTTOM, bottom)
                    .setValue(ScaffoldingBlock.WATERLOGGED, waterlogged)
                    .setValue(ScaffoldingBlock.DISTANCE, i);

            MODELS.put(state, Either.left(model));

            if (i != 7 && !(bottom && i == 0)) {
                list.add(state);
                SPECIAL_REMAPS.put(state, state.setValue(ScaffoldingBlock.DISTANCE, 7));
            }
        }
    }

    private static void addFenceGates(Block... blocks) {
        for (Block base : blocks) {
            addFenceGates(base, true, true, true, BlockModelType.GATE_NORTH_SOUTH_INWALL_OPEN);
            addFenceGates(base, true, true, false, BlockModelType.GATE_NORTH_SOUTH_INWALL);
            addFenceGates(base, true, false, true, BlockModelType.GATE_NORTH_SOUTH_OPEN);
            addFenceGates(base, true, false, false, BlockModelType.GATE_NORTH_SOUTH);
            addFenceGates(base, false, true, true, BlockModelType.GATE_EAST_WEST_INWALL_OPEN);
            addFenceGates(base, false, true, false, BlockModelType.GATE_EAST_WEST_INWALL);
            addFenceGates(base, false, false, true, BlockModelType.GATE_EAST_WEST_OPEN);
            addFenceGates(base, false, false, false, BlockModelType.GATE_EAST_WEST);
        }
    }

    private static void addFenceGates(Block base, boolean northSouth, boolean inWall, boolean open, BlockModelType modelType) {
        var list = USABLE_STATES.computeIfAbsent(modelType, x -> new ReferenceArrayList<>());

        var directions = northSouth ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.EAST, Direction.WEST};
        for (Direction direction : directions) {
            var state = base.defaultBlockState()
                    .setValue(FenceGateBlock.IN_WALL, inWall)
                    .setValue(FenceGateBlock.OPEN, open)
                    .setValue(FenceGateBlock.POWERED, true)
                    .setValue(FenceGateBlock.FACING, direction);
            list.add(state);
            SPECIAL_REMAPS.put(state, state.setValue(FenceGateBlock.POWERED, false));
        }
    }

    private static void generateDefault(BlockModelType type, Block... blocks) {
        generateDefault(type, (b) -> true, blocks);
    }

    private static void generateDefault(BlockModelType type, Predicate<BlockState> shouldInclude, Block... blocks) {
        var list = USABLE_STATES.computeIfAbsent(type, x -> new ReferenceArrayList<>());

        for (var block : blocks) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            var model = new PolymerBlockModel[]{PolymerBlockModel.of(Identifier.parse(id.getNamespace() + ":block/" + id.getPath()))};
            for (var state : block.getStateDefinition().getPossibleStates()) {
                MODELS.put(state, Either.left(model));
                if (shouldInclude.test(state)) {
                    list.add(state);
                }
            }

            if (block instanceof LeavesBlock) {
                list.remove(block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
                list.remove(block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true).setValue(LeavesBlock.WATERLOGGED, true));
            } else {
                list.remove(block.defaultBlockState());
            }
        }
    }
}
