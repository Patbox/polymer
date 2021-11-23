package eu.pb4.polymer.api.block;

import eu.pb4.polymer.api.utils.events.BooleanEvent;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class PolymerBlockUtils {
    private PolymerBlockUtils() {
    }

    public static final int NESTED_DEFAULT_DISTANCE = 32;
    public static final int BLOCK_STATE_OFFSET = Integer.MAX_VALUE / 64;

    public static final Predicate<BlockState> IS_POLYMER_BLOCK_STATE_PREDICATE = state -> state.getBlock() instanceof PolymerBlock;

    /**
     * This event allows you to force server side mining for any block/item
     */
    public static final BooleanEvent<MineEventListener> SERVER_SIDE_MINING_CHECK = new BooleanEvent<>();
    /**
     * This event allows you to force syncing of light updates between server and clinet
     */
    public static final BooleanEvent<BiPredicate<ServerWorld, ChunkSectionPos>> SEND_LIGHT_UPDATE_PACKET = new BooleanEvent<>();
    private static final Object2BooleanMap<BlockState> IS_LIGHT_SOURCE_CACHE = new Object2BooleanOpenHashMap<>();
    private static final HashSet<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashSet<>();

    /**
     * Marks BlockEntity type as server-side only
     *
     * @param types BlockEntityTypes
     */

    public static void registerBlockEntity(BlockEntityType<?>... types) {
        BLOCK_ENTITY_TYPES.addAll(Arrays.asList(types));
    }

    /**
     * Checks if BlockEntity is server-side only
     *
     * @param type BlockEntities type
     */
    public static boolean isRegisteredBlockEntity(BlockEntityType<?> type) {
        return BLOCK_ENTITY_TYPES.contains(type);
    }

    /**
     * This method is used internally to check if BlockState is PolymerBlock with light source not equal to it's visual block
     *
     * @param blockState
     * @return
     */
    public static boolean isLightSource(BlockState blockState) {
        if (blockState.getBlock() instanceof PolymerBlock virtualBlock) {
            if (virtualBlock.forceLightUpdates(blockState)) {
                return true;
            }

            if (PolymerBlockUtils.IS_LIGHT_SOURCE_CACHE.containsKey(blockState)) {
                return PolymerBlockUtils.IS_LIGHT_SOURCE_CACHE.getBoolean(blockState);
            } else {
                if (blockState.getLuminance() != virtualBlock.getPolymerBlockState(blockState).getLuminance()) {
                    PolymerBlockUtils.IS_LIGHT_SOURCE_CACHE.put(blockState, true);
                    return true;
                }

                PolymerBlockUtils.IS_LIGHT_SOURCE_CACHE.put(blockState, false);
                return false;
            }
        }
        return false;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState)} )} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param blockState  Server side BlockState
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance) {
        BlockState out = block.getPolymerBlockState(blockState);

        int req = 0;
        while (out.getBlock() instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBlockState(blockState);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState)} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block      PolymerBlock
     * @param blockState Server side BlockState
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE);
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlock(BlockState)} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param state       BlockState
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @return Client side BlockState
     */
    public static Block getBlockSafely(PolymerBlock block, BlockState state, int maxDistance) {
        int req = 0;
        Block out = block.getPolymerBlock(state);

        while (out instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBlock(state);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlock(BlockState)} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block PolymerBlock
     * @param state BlockState
     * @return Client side BlockState
     */
    public static Block getBlockSafely(PolymerBlock block, BlockState state) {
        return getBlockSafely(block, state, NESTED_DEFAULT_DISTANCE);
    }

    @FunctionalInterface
    public interface MineEventListener {
        boolean onBlockMine(ServerPlayerEntity player, BlockPos pos, BlockState state);
    }
}
