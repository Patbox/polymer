package eu.pb4.polymer.blocks.api;

import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.util.math.Direction;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public enum BlockModelType {
    FULL_BLOCK,
    TRANSPARENT_BLOCK,
    TRANSPARENT_BLOCK_WATERLOGGED,
    BIOME_TRANSPARENT_BLOCK,
    BIOME_TRANSPARENT_BLOCK_WATERLOGGED,
    FARMLAND_BLOCK,
    VINES_BLOCK,
    PLANT_BLOCK,
    BIOME_PLANT_BLOCK,
    KELP_BLOCK,
    CACTUS_BLOCK,
    TRIPWIRE_BLOCK,
    TRIPWIRE_BLOCK_FLAT,
    ACTIVE_PRESSURE_PLATE,
    HEAD,
    // Sculk
    SCULK_SENSOR_BLOCK,
    SCULK_SENSOR_BLOCK_WATERLOGGED,
    ACTIVE_SCULK_SENSOR_BLOCK,
    ACTIVE_SCULK_SENSOR_BLOCK_WATERLOGGED,
    // Slab
    TOP_SLAB,
    TOP_SLAB_WATERLOGGED,
    BOTTOM_SLAB,
    BOTTOM_SLAB_WATERLOGGED,
    // Campfire
    CAMPFIRE,
    CAMPFIRE_WATERLOGGED,
    // Trapdoor
    TOP_TRAPDOOR,
    BOTTOM_TRAPDOOR,
    NORTH_TRAPDOOR,
    EAST_TRAPDOOR,
    SOUTH_TRAPDOOR,
    WEST_TRAPDOOR,
    TOP_TRAPDOOR_WATERLOGGED,
    BOTTOM_TRAPDOOR_WATERLOGGED,
    NORTH_TRAPDOOR_WATERLOGGED,
    EAST_TRAPDOOR_WATERLOGGED,
    SOUTH_TRAPDOOR_WATERLOGGED,
    WEST_TRAPDOOR_WATERLOGGED,
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
    NORTH_DOOR,
    EAST_DOOR,
    SOUTH_DOOR,
    WEST_DOOR,
    // Shelf
    NORTH_SHELF,
    EAST_SHELF,
    SOUTH_SHELF,
    WEST_SHELF,
    NORTH_SHELF_WATERLOGGED,
    EAST_SHELF_WATERLOGGED,
    SOUTH_SHELF_WATERLOGGED,
    WEST_SHELF_WATERLOGGED,
    // Scaffolding
    TOP_SCAFFOLDING,
    BOTTOM_SCAFFOLDING,
    TOP_SCAFFOLDING_WATERLOGGED,
    BOTTOM_SCAFFOLDING_WATERLOGGED,
    // Gate
    NORTH_SOUTH_INWALL_GATE,
    NORTH_SOUTH_INWALL_OPEN_GATE,
    NORTH_SOUTH_GATE,
    NORTH_SOUTH_OPEN_GATE,
    EAST_WEST_INWALL_GATE,
    EAST_WEST_INWALL_OPEN_GATE,
    EAST_WEST_GATE,
    EAST_WEST_OPEN_GATE,
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
                ? (waterlogged ? SCULK_SENSOR_BLOCK_WATERLOGGED : SCULK_SENSOR_BLOCK)
                : (waterlogged ? ACTIVE_SCULK_SENSOR_BLOCK_WATERLOGGED : ACTIVE_SCULK_SENSOR_BLOCK);
    }

    public static BlockModelType getSlab(SlabType type, boolean waterlogged) {
        if (type == SlabType.DOUBLE) {
            return FULL_BLOCK;
        }
        return getSlab(type == SlabType.BOTTOM, waterlogged);
    }

    public static BlockModelType getSlab(boolean bottom, boolean waterlogged) {
        return bottom
                ? (waterlogged ? BOTTOM_SLAB_WATERLOGGED : BOTTOM_SLAB)
                : (waterlogged ? TOP_SLAB_WATERLOGGED : TOP_SLAB);
    }

    public static BlockModelType getTrapdoor(Direction direction, boolean waterlogged) {
        if (waterlogged) {
            return switch (direction) {
                case NORTH -> NORTH_TRAPDOOR_WATERLOGGED;
                case SOUTH -> SOUTH_TRAPDOOR_WATERLOGGED;
                case WEST -> WEST_TRAPDOOR_WATERLOGGED;
                case EAST -> EAST_TRAPDOOR_WATERLOGGED;
                case UP -> BOTTOM_TRAPDOOR_WATERLOGGED;
                case DOWN -> TOP_TRAPDOOR_WATERLOGGED;
            };
        }

        return switch (direction) {
            case NORTH -> NORTH_TRAPDOOR;
            case SOUTH -> SOUTH_TRAPDOOR;
            case WEST -> WEST_TRAPDOOR;
            case EAST -> EAST_TRAPDOOR;
            case UP -> BOTTOM_TRAPDOOR;
            case DOWN -> TOP_TRAPDOOR;
        };
    }

    public static BlockModelType getDoor(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_DOOR;
            case SOUTH -> SOUTH_DOOR;
            case WEST -> WEST_DOOR;
            case EAST -> EAST_DOOR;
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
                case NORTH -> NORTH_SHELF_WATERLOGGED;
                case SOUTH -> SOUTH_SHELF_WATERLOGGED;
                case WEST -> WEST_SHELF_WATERLOGGED;
                case EAST -> EAST_SHELF_WATERLOGGED;
                default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
            };
        }
        return switch (direction) {
            case NORTH -> NORTH_SHELF;
            case SOUTH -> SOUTH_SHELF;
            case WEST -> WEST_SHELF;
            case EAST -> EAST_SHELF;
            default -> throw new IllegalArgumentException("Only horizontal directions are supported!");
        };
    }

    public static BlockModelType getScaffolding(boolean bottom, boolean waterlogged) {
        return bottom
                ? (waterlogged ? BOTTOM_SCAFFOLDING_WATERLOGGED : BOTTOM_SCAFFOLDING)
                : (waterlogged ? TOP_SCAFFOLDING_WATERLOGGED : TOP_SCAFFOLDING);
    }

    public static BlockModelType getGate(Direction.Axis axis, boolean inwall, boolean open) {
        if (open) {
            if (inwall) {
                return switch (axis) {
                    case X -> NORTH_SOUTH_INWALL_OPEN_GATE;
                    case Z -> EAST_WEST_INWALL_OPEN_GATE;
                    default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
                };
            }
            return switch (axis) {
                case X -> NORTH_SOUTH_OPEN_GATE;
                case Z -> EAST_WEST_OPEN_GATE;
                default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
            };
        }

        if (inwall) {
            return switch (axis) {
                case X -> NORTH_SOUTH_INWALL_GATE;
                case Z -> EAST_WEST_INWALL_GATE;
                default -> throw new IllegalArgumentException("Only horizontal axis are supported!");
            };
        }
        return switch (axis) {
            case X -> NORTH_SOUTH_GATE;
            case Z -> EAST_WEST_GATE;
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
            id |= 1 << (dir.getIndex() - 1);
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

    public static BlockModelType getStairs(Direction direction, BlockHalf blockHalf, StairShape shape, boolean waterlogged) {
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