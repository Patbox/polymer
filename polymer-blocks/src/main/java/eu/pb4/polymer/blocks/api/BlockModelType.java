package eu.pb4.polymer.blocks.api;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

public enum BlockModelType {
    FULL_BLOCK,
    LEAVES,
    LEAVES_WATERLOGGED,
    BIOME_COLORED_LEAVES,
    BIOME_COLORED_LEAVES_WATERLOGGED,
    FARMLAND,
    VINES,
    PLANT,
    BIOME_PLANT,
    KELP,
    CACTUS,
    TRIPWIRE,
    TRIPWIRE_FLAT,
    PRESSURE_PLATE_ACTIVE,
    HEAD,
    // Sculk
    SCULK_SENSOR,
    SCULK_SENSOR_WATERLOGGED,
    SCULK_SENSOR_ACTIVE,
    SCULK_SENSOR_ACTIVE_WATERLOGGED,
    // Slab
    SLAB_TOP,
    SLAB_TOP_WATERLOGGED,
    SLAB_BOTTOM,
    SLAB_BOTTOM_WATERLOGGED,
    // Campfire
    CAMPFIRE,
    CAMPFIRE_WATERLOGGED,
    // Trapdoor
    TRAPDOOR_TOP,
    TRAPDOOR_BOTTOM,
    TRAPDOOR_NORTH,
    TRAPDOOR_EAST,
    TRAPDOOR_SOUTH,
    TRAPDOOR_WEST,
    TRAPDOOR_TOP_WATERLOGGED,
    TRAPDOOR_BOTTOM_WATERLOGGED,
    TRAPDOOR_NORTH_WATERLOGGED,
    TRAPDOOR_EAST_WATERLOGGED,
    TRAPDOOR_SOUTH_WATERLOGGED,
    TRAPDOOR_WEST_WATERLOGGED,
    // Lightning rod
    LIGHTNING_ROD_X,
    LIGHTNING_ROD_Y,
    LIGHTNING_ROD_Z,
    LIGHTNING_ROD_X_WATERLOGGED,
    LIGHTNING_ROD_Y_WATERLOGGED,
    LIGHTNING_ROD_Z_WATERLOGGED,
    // Chain
    CHAIN_X,
    CHAIN_Y,
    CHAIN_Z,
    CHAIN_X_WATERLOGGED,
    CHAIN_Y_WATERLOGGED,
    CHAIN_Z_WATERLOGGED,
    // Lantern
    LANTERN,
    LANTERN_HANGING,
    LANTERN_WATERLOGGED,
    LANTERN_HANGING_WATERLOGGED,
    // Door
    DOOR_NORTH,
    DOOR_EAST,
    DOOR_SOUTH,
    DOOR_WEST,
    // Shelf
    SHELF_NORTH,
    SHELF_EAST,
    SHELF_SOUTH,
    SHELF_WEST,
    SHELF_NORTH_WATERLOGGED,
    SHELF_EAST_WATERLOGGED,
    SHELF_SOUTH_WATERLOGGED,
    SHELF_WEST_WATERLOGGED,
    // Scaffolding
    SCAFFOLDING_TOP,
    SCAFFOLDING_TOP_WATERLOGGED,
    SCAFFOLDING_BOTTOM,
    SCAFFOLDING_BOTTOM_WATERLOGGED,
    // Gate
    GATE_NORTH_SOUTH_INWALL,
    GATE_NORTH_SOUTH_INWALL_OPEN,
    GATE_NORTH_SOUTH,
    GATE_NORTH_SOUTH_OPEN,
    GATE_EAST_WEST_INWALL,
    GATE_EAST_WEST_INWALL_OPEN,
    GATE_EAST_WEST,
    GATE_EAST_WEST_OPEN,
    // Bed
    BED_NORTH_HEAD,
    BED_NORTH_FOOT,
    BED_EAST_HEAD,
    BED_EAST_FOOT,
    BED_SOUTH_HEAD,
    BED_SOUTH_FOOT,
    BED_WEST_HEAD,
    BED_WEST_FOOT,
    // Bars
    BARS_CENTER,
    BARS_CENTER_WATERLOGGED,
    BARS_NORTH,
    BARS_NORTH_WATERLOGGED,
    BARS_SOUTH,
    BARS_SOUTH_WATERLOGGED,
    BARS_NORTH_SOUTH,
    BARS_NORTH_SOUTH_WATERLOGGED,
    BARS_WEST,
    BARS_WEST_WATERLOGGED,
    BARS_NORTH_WEST,
    BARS_NORTH_WEST_WATERLOGGED,
    BARS_SOUTH_WEST,
    BARS_SOUTH_WEST_WATERLOGGED,
    BARS_NORTH_SOUTH_WEST,
    BARS_NORTH_SOUTH_WEST_WATERLOGGED,
    BARS_EAST,
    BARS_EAST_WATERLOGGED,
    BARS_NORTH_EAST,
    BARS_NORTH_EAST_WATERLOGGED,
    BARS_EAST_SOUTH,
    BARS_EAST_SOUTH_WATERLOGGED,
    BARS_NORTH_EAST_SOUTH,
    BARS_NORTH_EAST_SOUTH_WATERLOGGED,
    BARS_EAST_WEST,
    BARS_EAST_WEST_WATERLOGGED,
    BARS_NORTH_EAST_WEST,
    BARS_NORTH_EAST_WEST_WATERLOGGED,
    BARS_EAST_SOUTH_WEST,
    BARS_EAST_SOUTH_WEST_WATERLOGGED,
    BARS_NORTH_EAST_SOUTH_WEST,
    BARS_NORTH_EAST_SOUTH_WEST_WATERLOGGED,
    // Stairs
    STAIRS_NORTH_TOP_STRAIGHT,
    STAIRS_NORTH_TOP_STRAIGHT_WATERLOGGED,
    STAIRS_NORTH_TOP_INNER_LEFT,
    STAIRS_NORTH_TOP_INNER_LEFT_WATERLOGGED,
    STAIRS_NORTH_TOP_INNER_RIGHT,
    STAIRS_NORTH_TOP_INNER_RIGHT_WATERLOGGED,
    STAIRS_NORTH_TOP_OUTER_LEFT,
    STAIRS_NORTH_TOP_OUTER_LEFT_WATERLOGGED,
    STAIRS_NORTH_TOP_OUTER_RIGHT,
    STAIRS_NORTH_TOP_OUTER_RIGHT_WATERLOGGED,
    STAIRS_NORTH_BOTTOM_STRAIGHT,
    STAIRS_NORTH_BOTTOM_STRAIGHT_WATERLOGGED,
    STAIRS_NORTH_BOTTOM_INNER_LEFT,
    STAIRS_NORTH_BOTTOM_INNER_LEFT_WATERLOGGED,
    STAIRS_NORTH_BOTTOM_INNER_RIGHT,
    STAIRS_NORTH_BOTTOM_INNER_RIGHT_WATERLOGGED,
    STAIRS_NORTH_BOTTOM_OUTER_LEFT,
    STAIRS_NORTH_BOTTOM_OUTER_LEFT_WATERLOGGED,
    STAIRS_NORTH_BOTTOM_OUTER_RIGHT,
    STAIRS_NORTH_BOTTOM_OUTER_RIGHT_WATERLOGGED,
    STAIRS_EAST_TOP_STRAIGHT,
    STAIRS_EAST_TOP_STRAIGHT_WATERLOGGED,
    STAIRS_EAST_TOP_INNER_LEFT,
    STAIRS_EAST_TOP_INNER_LEFT_WATERLOGGED,
    STAIRS_EAST_TOP_INNER_RIGHT,
    STAIRS_EAST_TOP_INNER_RIGHT_WATERLOGGED,
    STAIRS_EAST_TOP_OUTER_LEFT,
    STAIRS_EAST_TOP_OUTER_LEFT_WATERLOGGED,
    STAIRS_EAST_TOP_OUTER_RIGHT,
    STAIRS_EAST_TOP_OUTER_RIGHT_WATERLOGGED,
    STAIRS_EAST_BOTTOM_STRAIGHT,
    STAIRS_EAST_BOTTOM_STRAIGHT_WATERLOGGED,
    STAIRS_EAST_BOTTOM_INNER_LEFT,
    STAIRS_EAST_BOTTOM_INNER_LEFT_WATERLOGGED,
    STAIRS_EAST_BOTTOM_INNER_RIGHT,
    STAIRS_EAST_BOTTOM_INNER_RIGHT_WATERLOGGED,
    STAIRS_EAST_BOTTOM_OUTER_LEFT,
    STAIRS_EAST_BOTTOM_OUTER_LEFT_WATERLOGGED,
    STAIRS_EAST_BOTTOM_OUTER_RIGHT,
    STAIRS_EAST_BOTTOM_OUTER_RIGHT_WATERLOGGED,
    STAIRS_SOUTH_TOP_STRAIGHT,
    STAIRS_SOUTH_TOP_STRAIGHT_WATERLOGGED,
    STAIRS_SOUTH_TOP_INNER_LEFT,
    STAIRS_SOUTH_TOP_INNER_LEFT_WATERLOGGED,
    STAIRS_SOUTH_TOP_INNER_RIGHT,
    STAIRS_SOUTH_TOP_INNER_RIGHT_WATERLOGGED,
    STAIRS_SOUTH_TOP_OUTER_LEFT,
    STAIRS_SOUTH_TOP_OUTER_LEFT_WATERLOGGED,
    STAIRS_SOUTH_TOP_OUTER_RIGHT,
    STAIRS_SOUTH_TOP_OUTER_RIGHT_WATERLOGGED,
    STAIRS_SOUTH_BOTTOM_STRAIGHT,
    STAIRS_SOUTH_BOTTOM_STRAIGHT_WATERLOGGED,
    STAIRS_SOUTH_BOTTOM_INNER_LEFT,
    STAIRS_SOUTH_BOTTOM_INNER_LEFT_WATERLOGGED,
    STAIRS_SOUTH_BOTTOM_INNER_RIGHT,
    STAIRS_SOUTH_BOTTOM_INNER_RIGHT_WATERLOGGED,
    STAIRS_SOUTH_BOTTOM_OUTER_LEFT,
    STAIRS_SOUTH_BOTTOM_OUTER_LEFT_WATERLOGGED,
    STAIRS_SOUTH_BOTTOM_OUTER_RIGHT,
    STAIRS_SOUTH_BOTTOM_OUTER_RIGHT_WATERLOGGED,
    STAIRS_WEST_TOP_STRAIGHT,
    STAIRS_WEST_TOP_STRAIGHT_WATERLOGGED,
    STAIRS_WEST_TOP_INNER_LEFT,
    STAIRS_WEST_TOP_INNER_LEFT_WATERLOGGED,
    STAIRS_WEST_TOP_INNER_RIGHT,
    STAIRS_WEST_TOP_INNER_RIGHT_WATERLOGGED,
    STAIRS_WEST_TOP_OUTER_LEFT,
    STAIRS_WEST_TOP_OUTER_LEFT_WATERLOGGED,
    STAIRS_WEST_TOP_OUTER_RIGHT,
    STAIRS_WEST_TOP_OUTER_RIGHT_WATERLOGGED,
    STAIRS_WEST_BOTTOM_STRAIGHT,
    STAIRS_WEST_BOTTOM_STRAIGHT_WATERLOGGED,
    STAIRS_WEST_BOTTOM_INNER_LEFT,
    STAIRS_WEST_BOTTOM_INNER_LEFT_WATERLOGGED,
    STAIRS_WEST_BOTTOM_INNER_RIGHT,
    STAIRS_WEST_BOTTOM_INNER_RIGHT_WATERLOGGED,
    STAIRS_WEST_BOTTOM_OUTER_LEFT,
    STAIRS_WEST_BOTTOM_OUTER_LEFT_WATERLOGGED,
    STAIRS_WEST_BOTTOM_OUTER_RIGHT,
    STAIRS_WEST_BOTTOM_OUTER_RIGHT_WATERLOGGED,
    ;


    public static BlockModelType getSculkSensor(boolean active, boolean waterlogged) {
        return active
                ? (waterlogged ? SCULK_SENSOR_WATERLOGGED : SCULK_SENSOR)
                : (waterlogged ? SCULK_SENSOR_ACTIVE_WATERLOGGED : SCULK_SENSOR_ACTIVE);
    }

    public static BlockModelType getSlab(SlabType type, boolean waterlogged) {
        if (type == SlabType.DOUBLE) {
            return FULL_BLOCK;
        }
        return getSlab(type == SlabType.BOTTOM, waterlogged);
    }

    public static BlockModelType getSlab(boolean bottom, boolean waterlogged) {
        return bottom
                ? (waterlogged ? SLAB_BOTTOM_WATERLOGGED : SLAB_BOTTOM)
                : (waterlogged ? SLAB_TOP_WATERLOGGED : SLAB_TOP);
    }

    public static BlockModelType getTrapdoor(Direction direction, boolean waterlogged) {
        if (waterlogged) {
            return switch (direction) {
                case NORTH -> TRAPDOOR_NORTH_WATERLOGGED;
                case SOUTH -> TRAPDOOR_SOUTH_WATERLOGGED;
                case WEST -> TRAPDOOR_WEST_WATERLOGGED;
                case EAST -> TRAPDOOR_EAST_WATERLOGGED;
                case UP -> TRAPDOOR_BOTTOM_WATERLOGGED;
                case DOWN -> TRAPDOOR_TOP_WATERLOGGED;
            };
        }

        return switch (direction) {
            case NORTH -> TRAPDOOR_NORTH;
            case SOUTH -> TRAPDOOR_SOUTH;
            case WEST -> TRAPDOOR_WEST;
            case EAST -> TRAPDOOR_EAST;
            case UP -> TRAPDOOR_BOTTOM;
            case DOWN -> TRAPDOOR_TOP;
        };
    }

    public static BlockModelType getDoor(Direction direction) {
        return switch (direction) {
            case NORTH -> DOOR_NORTH;
            case SOUTH -> DOOR_SOUTH;
            case WEST -> DOOR_WEST;
            case EAST -> DOOR_EAST;
            default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
        };
    }

    public static BlockModelType getLightningRod(Direction.Axis axis, boolean waterlogged) {
        if (waterlogged) {
            return switch (axis) {
                case X -> LIGHTNING_ROD_X_WATERLOGGED;
                case Y -> LIGHTNING_ROD_Y_WATERLOGGED;
                case Z -> LIGHTNING_ROD_Z_WATERLOGGED;
            };
        }

        return switch (axis) {
            case X -> LIGHTNING_ROD_X;
            case Y -> LIGHTNING_ROD_Y;
            case Z -> LIGHTNING_ROD_Z;
        };
    }

    public static BlockModelType getChain(Direction.Axis axis, boolean waterlogged) {
        if (waterlogged) {
            return switch (axis) {
                case X -> CHAIN_X_WATERLOGGED;
                case Y -> CHAIN_Y_WATERLOGGED;
                case Z -> CHAIN_Z_WATERLOGGED;
            };
        }

        return switch (axis) {
            case X -> CHAIN_X;
            case Y -> CHAIN_Y;
            case Z -> CHAIN_Z;
        };
    }

    public static BlockModelType getLantern(boolean hanging, boolean waterlogged) {
        return hanging
                ? (waterlogged ? LANTERN_HANGING_WATERLOGGED : LANTERN_HANGING)
                : (waterlogged ? LANTERN_WATERLOGGED : LANTERN);
    }

    public static BlockModelType getShelf(Direction direction, boolean waterlogged) {
        if (waterlogged) {
            return switch (direction) {
                case NORTH -> SHELF_NORTH_WATERLOGGED;
                case SOUTH -> SHELF_SOUTH_WATERLOGGED;
                case WEST -> SHELF_WEST_WATERLOGGED;
                case EAST -> SHELF_EAST_WATERLOGGED;
                default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
            };
        }
        return switch (direction) {
            case NORTH -> SHELF_NORTH;
            case SOUTH -> SHELF_SOUTH;
            case WEST -> SHELF_WEST;
            case EAST -> SHELF_EAST;
            default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
        };
    }

    public static BlockModelType getBed(Direction direction, BedPart part) {
        return getBed(direction, part == BedPart.HEAD);
    }
    public static BlockModelType getBed(Direction direction, boolean head) {
        if (head) {
            return switch (direction) {
                case NORTH -> BED_NORTH_HEAD;
                case SOUTH -> BED_SOUTH_HEAD;
                case WEST -> BED_WEST_HEAD;
                case EAST -> BED_EAST_HEAD;
                default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
            };
        }
        return switch (direction) {
            case NORTH -> BED_NORTH_FOOT;
            case SOUTH -> BED_SOUTH_FOOT;
            case WEST -> BED_WEST_FOOT;
            case EAST -> BED_EAST_FOOT;
            default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
        };
    }

    public static BlockModelType getScaffolding(boolean bottom, boolean waterlogged) {
        return bottom
                ? (waterlogged ? SCAFFOLDING_BOTTOM_WATERLOGGED : SCAFFOLDING_BOTTOM)
                : (waterlogged ? SCAFFOLDING_TOP_WATERLOGGED : SCAFFOLDING_TOP);
    }

    public static BlockModelType getGate(Direction.Axis axis, boolean inwall, boolean open) {
        if (open) {
            if (inwall) {
                return switch (axis) {
                    case X -> GATE_NORTH_SOUTH_INWALL_OPEN;
                    case Z -> GATE_EAST_WEST_INWALL_OPEN;
                    default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
                };
            }
            return switch (axis) {
                case X -> GATE_NORTH_SOUTH_OPEN;
                case Z -> GATE_EAST_WEST_OPEN;
                default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
            };
        }

        if (inwall) {
            return switch (axis) {
                case X -> GATE_NORTH_SOUTH_INWALL;
                case Z -> GATE_EAST_WEST_INWALL;
                default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
            };
        }
        return switch (axis) {
            case X -> GATE_NORTH_SOUTH;
            case Z -> GATE_EAST_WEST;
            default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
        };
    }

    public static BlockModelType getBars(boolean waterlogged, Direction... directions) {
        return getBars(waterlogged, List.of(directions));
    }
    public static BlockModelType getBars(boolean waterlogged, Collection<Direction> directions) {
        int id = 0;
        if (waterlogged) {
            id |= 1;
        }
        for (var dir : directions) {
            if (dir.getAxis().isVertical()) {
                throw new IllegalArgumentException("Only horizontal directions are supported!");
            }
            id |= 1 << (dir.get3DDataValue() - 1);
        }

        return BlockModelType.values()[BARS_CENTER.ordinal() + id];
    }

    public static BlockModelType getBars(boolean waterlogged, boolean north, boolean south, boolean west, boolean east) {
        var set = EnumSet.noneOf(Direction.class);
        if (north) set.add(Direction.NORTH);
        if (south) set.add(Direction.SOUTH);
        if (west) set.add(Direction.WEST);
        if (east) set.add(Direction.EAST);
        return getBars(waterlogged, set);
    }

    public static BlockModelType getStairs(Direction direction, Half blockHalf, StairsShape shape, boolean waterlogged) {
        if (direction.getAxis().isVertical()) {
            throw new IllegalArgumentException("Only horizontal directions are supported!");
        }

        var self = new StringBuilder();
        self.append("STAIRS_").append(direction.name())
                .append("_").append(blockHalf.name())
                .append("_").append(shape.name());


        if (waterlogged) {
            self.append("_WATERLOGGED");
        }

        // Bit ugly, but works
        return BlockModelType.valueOf(self.toString());
    }
}