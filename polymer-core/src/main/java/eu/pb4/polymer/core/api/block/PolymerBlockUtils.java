package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import eu.pb4.polymer.core.mixin.block.BlockEntityUpdateS2CPacketAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import io.github.theepicblock.polymc.PolyMc;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class PolymerBlockUtils {
    private static final NbtCompound STATIC_COMPOUND = new NbtCompound();

    private PolymerBlockUtils() {
    }

    public static final int NESTED_DEFAULT_DISTANCE = 32;

    public static final Predicate<BlockState> IS_POLYMER_BLOCK_STATE_PREDICATE = state -> state.getBlock() instanceof PolymerBlock;

    /**
     * This event allows you to force server side mining for any block/item
     */
    public static final BooleanEvent<MineEventListener> SERVER_SIDE_MINING_CHECK = new BooleanEvent<>();

    public static final SimpleEvent<BreakingProgressListener> BREAKING_PROGRESS_UPDATE = new SimpleEvent<>();

    /**
     * This event allows you to force syncing of light updates between server and clinet
     */
    public static final BooleanEvent<BiPredicate<ServerWorld, ChunkSectionPos>> SEND_LIGHT_UPDATE_PACKET = new BooleanEvent<>();
    private static final Set<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    private static boolean requireStrictBlockUpdates = false;

    /**
     * Marks BlockEntity type as server-side only
     *
     * @param types BlockEntityTypes
     */
    public static void registerBlockEntity(BlockEntityType<?>... types) {
        BLOCK_ENTITY_TYPES.addAll(Arrays.asList(types));

        for (var type : types) {
            RegistrySyncUtils.setServerEntry(Registries.BLOCK_ENTITY_TYPE, type);
        }
    }

    /**
     * Checks if BlockEntity is server-side only
     *
     * @param type BlockEntities type
     */
    public static boolean isPolymerBlockEntityType(BlockEntityType<?> type) {
        return BLOCK_ENTITY_TYPES.contains(type);
    }

    /**
     * This method is used to check if BlockState should force sending of light updates to client
     *
     * @param blockState
     * @return
     */
    public static boolean forceLightUpdates(BlockState blockState) {
        if (blockState.getBlock() instanceof PolymerBlock virtualBlock) {
            if (virtualBlock.forceLightUpdates(blockState)) {
                return true;
            }

            return ((BlockStateExtra) blockState).polymer$isPolymerLightSource();
        }
        return false;
    }

    /**
     * Gets BlockState used on client side
     *
     * @param state server side BlockState
     * @return Client side BlockState
     */
    public static BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlockState(state, null);
    }

    /**
     * Gets BlockState used on client side
     *
     * @param state server side BlockState
     * @param player      Possible target player
     * @return Client side BlockState
     */
    public static BlockState getPolymerBlockState(BlockState state, @Nullable ServerPlayerEntity player) {
        return BlockMapper.getFrom(player).toClientSideState(state, player);
    }

    public static Block getPolymerBlock(Block block, @Nullable ServerPlayerEntity player) {
        return BlockMapper.getFrom(player).toClientSideBlock(block, player);
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
            out = newBlock.getPolymerBlockState(out);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState)} )} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param blockState  Server side BlockState
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @param player      Possible target player
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, @Nullable ServerPlayerEntity player) {
        if (player == null) {
            return getBlockStateSafely(block, blockState, maxDistance);
        }

        BlockState out = block.getPolymerBlockState(blockState, player);

        int req = 0;
        while (out.getBlock() instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBlockState(blockState, player);
            req++;
        }
        return out;
    }

    public static BlockState getBlockBreakBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, @Nullable ServerPlayerEntity player) {
        if (player == null) {
            return getBlockStateSafely(block, blockState, maxDistance);
        }

        BlockState out = block.getPolymerBreakEventBlockState(blockState, player);

        int req = 0;
        while (out.getBlock() instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBreakEventBlockState(blockState, player);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState)} )} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param blockState  Server side BlockState
     * @param player      Possible target player
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, @Nullable ServerPlayerEntity player) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE, player);
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
     * @param player      Possible target player
     * @return Client side BlockState
     */
    public static Block getBlockSafely(PolymerBlock block, BlockState state, int maxDistance, @Nullable ServerPlayerEntity player) {
        Block out = (Block) block;
        if(player == null || !block.canSyncRawToClient(player)) {
            int req = 0;
            out = player != null
                    ? block.getPolymerBlock(state, player)
                    : block.getPolymerBlock(state);

            while (out instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
                out = player != null
                        ? newBlock.getPolymerBlock(out.getDefaultState(), player)
                        : newBlock.getPolymerBlock(state);
                req++;
            }
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlock(BlockState)} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param state       BlockState
     * @param player      Possible target player
     * @return Client side BlockState
     */
    public static Block getBlockSafely(PolymerBlock block, BlockState state, @Nullable ServerPlayerEntity player) {
        return getBlockSafely(block, state, NESTED_DEFAULT_DISTANCE, player);
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
        return getBlockSafely(block, state, maxDistance, null);
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

    public static BlockEntityUpdateS2CPacket createBlockEntityPacket(BlockPos pos, BlockEntityType<?> type, @Nullable NbtCompound nbtCompound) {
        return BlockEntityUpdateS2CPacketAccessor.createBlockEntityUpdateS2CPacket(pos.toImmutable(), type, nbtCompound != null ? nbtCompound : STATIC_COMPOUND);
    }

    @ApiStatus.Experimental
    public static void requireStrictBlockUpdates() {
        requireStrictBlockUpdates = true;
    }

    public static boolean isStrictBlockUpdateRequired() {
        return requireStrictBlockUpdates;
    }

    public static boolean shouldMineServerSide(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        return (state.getBlock() instanceof PolymerBlock block && block.handleMiningOnServer(player.getMainHandStack(), state, pos, player))
                || (player.getMainHandStack().getItem() instanceof PolymerItem item && item.handleMiningOnServer(player.getMainHandStack(), state, pos, player))
                || PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.invoke((x) -> x.onBlockMine(state, pos, player));
    }

    public static BlockState getServerSideBlockState(BlockState state, ServerPlayerEntity player) {
        return PolyMcUtils.toVanilla(getPolymerBlockState(state, player), player);
    }

    @FunctionalInterface
    public interface MineEventListener {
        boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface BreakingProgressListener {
        boolean onBreakingProgressUpdate(ServerPlayerEntity player, BlockPos pos, BlockState finalState, int i);
    }
}
