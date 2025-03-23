package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.common.impl.CommonImplUtils;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.compat.polymc.PolyMcUtils;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.mixin.block.BlockEntityUpdateS2CPacketAccessor;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public final class PolymerBlockUtils {
    public static final int NESTED_DEFAULT_DISTANCE = 32;
    public static final Predicate<BlockState> IS_POLYMER_BLOCK_STATE_PREDICATE = state -> PolymerSyncedObject.getSyncedObject(Registries.BLOCK, state.getBlock()) instanceof PolymerBlock;
    /**
     * This event allows you to force server side mining for any block/item
     */
    public static final BooleanEvent<MineEventListener> SERVER_SIDE_MINING_CHECK = new BooleanEvent<>();
    public static final SimpleEvent<BreakingProgressListener> BREAKING_PROGRESS_UPDATE = new SimpleEvent<>();
    public static final BooleanEvent<PolymerBlockInteractionListener> POLYMER_BLOCK_INTERACTION_CHECK = new BooleanEvent<>();
    /**
     * This event allows you to force syncing of light updates between server and clinet
     */
    public static final BooleanEvent<BiPredicate<ServerWorld, ChunkSectionPos>> SEND_LIGHT_UPDATE_PACKET = new BooleanEvent<>();
    private static final NbtCompound STATIC_COMPOUND = new NbtCompound();
    private static final Set<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new ObjectOpenCustomHashSet<>(CommonImplUtils.IDENTITY_HASH);

    private PolymerBlockUtils() {
    }

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
        if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, blockState.getBlock()) instanceof PolymerBlock virtualBlock) {
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
     * @param context context
     * @return Client side BlockState
     */
    public static BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return BlockMapper.getFrom(context.getPlayer()).toClientSideState(state, context);
    }

    public static Block getPolymerBlock(Block block, PacketContext context) {
        return BlockMapper.getFrom(context.getPlayer()).toClientSideState(block.getDefaultState(), context).getBlock();
    }

    public static void registerOverlay(Block block, PolymerBlock polymerBlock) {
        PolymerSyncedObject.setSyncedObject(Registries.BLOCK, block, polymerBlock);
        RegistrySyncUtils.setServerEntry(Registries.BLOCK, block);
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState, PacketContext)} )} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param blockState  Server side BlockState
     * @param maxDistance Maximum number of checks for nested virtual blocks
     * @param context Packet context
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, PacketContext context) {
        BlockState out = block.getPolymerBlockState(blockState, context);

        int req = 0;
        while (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, out.getBlock()) instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBlockState(out, context);
            req++;
        }
        return out;
    }

    public static BlockState getBlockBreakBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, PacketContext context) {
        BlockState out = block.getPolymerBreakEventBlockState(blockState, context);

        int req = 0;
        while (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, out.getBlock()) instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBreakEventBlockState(blockState, context);
            req++;
        }
        return out;
    }

    /**
     * This method is minimal wrapper around {@link PolymerBlock#getPolymerBlockState(BlockState, PacketContext)} )} to make sure
     * It gets replaced if it represents other PolymerBlock
     *
     * @param block       PolymerBlock
     * @param blockState  Server side BlockState
     * @param context      Possible target player
     * @return Client side BlockState
     */
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, PacketContext context) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE, context);
    }

    public static BlockEntityUpdateS2CPacket createBlockEntityPacket(BlockPos pos, BlockEntityType<?> type, @Nullable NbtCompound nbtCompound) {
        return BlockEntityUpdateS2CPacketAccessor.createBlockEntityUpdateS2CPacket(pos.toImmutable(), type, nbtCompound != null ? nbtCompound : STATIC_COMPOUND);
    }

    public static boolean shouldMineServerSide(ServerPlayerEntity player, BlockPos pos, BlockState state) {
        return (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, state.getBlock()) instanceof PolymerBlock block && block.handleMiningOnServer(player.getMainHandStack(), state, pos, player))
                || (PolymerSyncedObject.getSyncedObject(Registries.ITEM, player.getMainHandStack().getItem()) instanceof PolymerItem item && item.handleMiningOnServer(player.getMainHandStack(), state, pos, player))
                || PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.invoke((x) -> x.onBlockMine(state, pos, player));
    }

    public static BlockState getServerSideBlockState(BlockState state, PacketContext context) {
        return PolyMcUtils.toVanilla(getPolymerBlockState(state, context), context.getPlayer());
    }

    public static NbtCompound transformBlockEntityNbt(PacketContext context, BlockEntityType<?> type, NbtCompound original) {
        return PacketPatcher.transformBlockEntityNbt(context, type, original);
    }

    public static boolean isPolymerBlockInteraction(ServerPlayerEntity player, ItemStack stack, Hand hand, BlockState preInteractionState, BlockHitResult blockHitResult, ServerWorld world, ActionResult actionResult) {
        var blockState = world.getBlockState(blockHitResult.getBlockPos());
        if (PolymerSyncedObject.getSyncedObject(Registries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock && polymerBlock.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        } else if (!blockState.isOf(preInteractionState.getBlock()) && PolymerSyncedObject.getSyncedObject(Registries.BLOCK, preInteractionState.getBlock()) instanceof PolymerBlock polymerBlock && polymerBlock.isPolymerBlockInteraction(preInteractionState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        } else if (PolymerSyncedObject.getSyncedObject(Registries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        }

        return POLYMER_BLOCK_INTERACTION_CHECK.invoke(x -> x.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult));
    }

    @FunctionalInterface
    public interface MineEventListener {
        boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayerEntity player);
    }

    @FunctionalInterface
    public interface BreakingProgressListener {
        void onBreakingProgressUpdate(ServerPlayerEntity player, BlockPos pos, BlockState finalState, int i);
    }

    @FunctionalInterface
    public interface PolymerBlockInteractionListener {
        boolean isPolymerBlockInteraction(BlockState state, ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, BlockHitResult blockHitResult, ActionResult actionResult);
    }
}
