package eu.pb4.polymer.block;

import eu.pb4.polymer.other.DoubleBooleanEvent;
import eu.pb4.polymer.other.MineEvent;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

public final class BlockHelper {
    public static final int NESTED_DEFAULT_DISTANCE = 32;

    public static final Predicate<BlockState> IS_VIRTUAL_BLOCK_STATE_PREDICATE = state -> state.getBlock() instanceof VirtualBlock;

    /**
     * This event allows you to force server side mining for any block/item
     */
    public static final MineEvent SERVER_SIDE_MINING_CHECK = new MineEvent();
    /**
     * This event allows you to force syncing of light updates between server and clinet
     */
    public static final DoubleBooleanEvent<ServerWorld, ChunkSectionPos> SEND_LIGHT_UPDATE_PACKET = new DoubleBooleanEvent<>();
    private static final Object2BooleanMap<BlockState> IS_LIGHT_SOURCE_CACHE = new Object2BooleanOpenHashMap<>();

    private static final HashSet<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashSet<>();

    /**
     * Marks BlockEntity type as server-side only
     *
     * @param types BlockEntityTypes
     */

    public static void registerVirtualBlockEntity(BlockEntityType<?>... types) {
        BLOCK_ENTITY_TYPES.addAll(Arrays.asList(types));
    }

    /**
     * Checks if BlockEntity is server-side only
     *
     * @param type BlockEntities type
     */
    public static boolean isVirtualBlockEntity(BlockEntityType<?> type) {
        return BLOCK_ENTITY_TYPES.contains(type);
    }

    /**
     * This method is used internally to check if BlockState is VirtualBlock with light source not equal to it's visual block
     *
     * @param blockState
     * @return
     */
    public static boolean isVirtualLightSource(BlockState blockState) {
        if (blockState.getBlock() instanceof VirtualBlock virtualBlock) {
            if (virtualBlock.forceLightUpdates()) {
                return true;
            }

            if (BlockHelper.IS_LIGHT_SOURCE_CACHE.containsKey(blockState)) {
                return BlockHelper.IS_LIGHT_SOURCE_CACHE.getBoolean(blockState);
            } else {
                if (blockState.getLuminance() != virtualBlock.getVirtualBlockState(blockState).getLuminance()) {
                    BlockHelper.IS_LIGHT_SOURCE_CACHE.put(blockState, true);
                    return true;
                }

                BlockHelper.IS_LIGHT_SOURCE_CACHE.put(blockState, false);
                return false;
            }
        }
        return false;
    }

    /**
     * This method is minimal wrapper around {@link VirtualBlock#getVirtualBlockState(BlockState)} to make sure
     * It gets replaced if it represents other VirtualBlock
     *
     * @param block       VirtualBlock
     * @param blockState  Server side BlockState
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(VirtualBlock block, BlockState blockState, int maxDistance) {
        BlockState out = block.getVirtualBlockState(blockState);

        int req = 0;
        while (out.getBlock() instanceof VirtualBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getVirtualBlockState(out);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link VirtualBlock#getVirtualBlockState(BlockState)} to make sure
     * It gets replaced if it represents other VirtualBlock
     *
     * @param block      VirtualBlock
     * @param blockState Server side BlockState
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(VirtualBlock block, BlockState blockState) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE);
    }


    /**
     * This method is minimal wrapper around {@link VirtualBlock#getVirtualBlock(BlockPos pos, World world)} to make sure
     * It gets replaced if it represents other VirtualBlock
     *
     * @param block       VirtualBlock
     * @param world       Block's world
     * @param pos         Block's position
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side BlockState
     */
    public static Block getBlockSafely(VirtualBlock block, World world, BlockPos pos, int maxDistance) {
        int req = 0;
        Block out = block.getVirtualBlock(pos, world);

        while (out instanceof VirtualBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getVirtualBlock(pos, world);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link VirtualBlock#getVirtualBlock(BlockPos pos, World world)} to make sure
     * It gets replaced if it represents other VirtualBlock
     *
     * @param block VirtualBlock
     * @param world Block's world
     * @param pos   Block's position
     * @return Client side BlockState
     */
    public static Block getBlockSafely(VirtualBlock block, World world, BlockPos pos) {
        return getBlockSafely(block, world, pos, NESTED_DEFAULT_DISTANCE);
    }

    @Deprecated
    public static void registerVirtualBlockEntity(Identifier identifier) {
        BLOCK_ENTITY_TYPES.add(Registry.BLOCK_ENTITY_TYPE.get(identifier));
    }

    @Deprecated
    public static boolean isVirtualBlockEntity(Identifier identifier) {
        return BLOCK_ENTITY_TYPES.contains(Registry.BLOCK_ENTITY_TYPE.get(identifier));
    }

    @Deprecated
    public static boolean isVirtualBlockEntity(String identifier) {
        return isVirtualBlockEntity(Identifier.tryParse(identifier));
    }
}
