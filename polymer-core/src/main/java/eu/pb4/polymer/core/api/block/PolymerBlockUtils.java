package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.common.api.events.BooleanEvent;
import eu.pb4.polymer.common.api.events.SimpleEvent;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.interfaces.BlockStateExtra;
import eu.pb4.polymer.core.impl.networking.PacketPatcher;
import eu.pb4.polymer.core.mixin.block.ClientboundBlockEntityDataPacketAccessor;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class PolymerBlockUtils {
    public static final int NESTED_DEFAULT_DISTANCE = 32;
    public static final Predicate<BlockState> IS_POLYMER_BLOCK_STATE_PREDICATE = state -> PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock;
    /**
     * This event allows you to force server side mining for any block/item
     */
    public static final BooleanEvent<MineEventListener> SERVER_SIDE_MINING_CHECK = new BooleanEvent<>();
    public static final SimpleEvent<BreakingProgressListener> BREAKING_PROGRESS_UPDATE = new SimpleEvent<>();
    public static final BooleanEvent<PolymerBlockInteractionListener> POLYMER_BLOCK_INTERACTION_CHECK = new BooleanEvent<>();
    public static final BooleanEvent<PolymerIgnoreSoundExceptionListener> POLYMER_IGNORE_SOUND_EXCEPTED_ENTITY = new BooleanEvent<>();
    /**
     * This event allows you to force syncing of light updates between server and clinet
     */
    public static final BooleanEvent<BiPredicate<ServerLevel, SectionPos>> SEND_LIGHT_UPDATE_PACKET = new BooleanEvent<>();
    private static final CompoundTag STATIC_COMPOUND = new CompoundTag();
    private PolymerBlockUtils() {
    }

    /**
     * Marks BlockEntity type as server-side only
     *
     * @param types BlockEntityTypes
     */
    public static void registerBlockEntity(BlockEntityType<?>... types) {
        for (var type : types) {
            registerBlockEntity(type, (obj, ctx) -> null);
        }
    }

    public static void registerBlockEntity(BlockEntityType<?> type, PolymerSyncedObject<BlockEntityType<?>> syncedObject) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.BLOCK_ENTITY_TYPE, type, syncedObject);
    }

    /**
     * Checks if BlockEntity is server-side only
     *
     * @param type BlockEntities type
     */
    public static boolean isPolymerBlockEntityType(BlockEntityType<?> type) {
        return PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK_ENTITY_TYPE, type) != null;
    }

    /**
     * This method is used to check if BlockState should force sending of light updates to client
     *
     * @param blockState
     * @return
     */
    public static boolean forceLightUpdates(BlockState blockState) {
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, blockState.getBlock()) instanceof PolymerBlock virtualBlock) {
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
        return BlockMapper.getFrom(PolymerCommonUtils.getPlayer(context)).toClientSideState(state, context);
    }

    public static Block getPolymerBlock(Block block, PacketContext context) {
        return BlockMapper.getFrom(PolymerCommonUtils.getPlayer(context)).toClientSideState(block.defaultBlockState(), context).getBlock();
    }

    public static void registerOverlay(Block block, PolymerBlock polymerBlock) {
        PolymerBlock.registerOverlay(block, polymerBlock);
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
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, @Nullable PacketContext context) {
        BlockState out = block.getPolymerBlockState(blockState, context);

        int req = 0;
        while (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, out.getBlock()) instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
            out = newBlock.getPolymerBlockState(out, context);
            req++;
        }
        return out;
    }

    public static BlockState getBlockBreakBlockStateSafely(PolymerBlock block, BlockState blockState, int maxDistance, PacketContext context) {
        BlockState out = block.getPolymerBreakEventBlockState(blockState, context);

        int req = 0;
        while (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, out.getBlock()) instanceof PolymerBlock newBlock && newBlock != block && req < maxDistance) {
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
    public static BlockState getBlockStateSafely(PolymerBlock block, BlockState blockState, @Nullable PacketContext context) {
        return getBlockStateSafely(block, blockState, NESTED_DEFAULT_DISTANCE, context);
    }

    public static ClientboundBlockEntityDataPacket createBlockEntityPacket(BlockPos pos, BlockEntityType<?> type, @Nullable CompoundTag nbtCompound) {
        return ClientboundBlockEntityDataPacketAccessor.createBlockEntityUpdateS2CPacket(pos.immutable(), type, nbtCompound != null ? nbtCompound : STATIC_COMPOUND);
    }

    public static boolean shouldMineServerSide(ServerPlayer player, BlockPos pos, BlockState state) {
        return (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock block && block.handleMiningOnServer(player.getMainHandItem(), state, pos, player))
                || (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, player.getMainHandItem().getItem()) instanceof PolymerItem item && item.handleMiningOnServer(player.getMainHandItem(), state, pos, player))
                || PolymerBlockUtils.SERVER_SIDE_MINING_CHECK.invoke((x) -> x.onBlockMine(state, pos, player));
    }

    public static CompoundTag transformBlockEntityNbt(PacketContext context, BlockEntityType<?> type, CompoundTag original, HolderLookup.Provider lookup) {
        return PacketPatcher.transformBlockEntityNbt(context, type, original, lookup);
    }

    public static boolean isPolymerBlockInteraction(ServerPlayer player, ItemStack stack, InteractionHand hand, BlockState preInteractionState, BlockHitResult blockHitResult, ServerLevel world, InteractionResult actionResult) {
        var blockState = world.getBlockState(blockHitResult.getBlockPos());
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, blockState.getBlock()) instanceof PolymerBlock polymerBlock && polymerBlock.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        } else if (!blockState.is(preInteractionState.getBlock()) && PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, preInteractionState.getBlock()) instanceof PolymerBlock polymerBlock && polymerBlock.isPolymerBlockInteraction(preInteractionState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        } else if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult)) {
            return true;
        }

        return POLYMER_BLOCK_INTERACTION_CHECK.invoke(x -> x.isPolymerBlockInteraction(blockState, player, hand, stack, world, blockHitResult, actionResult));
    }

    public static boolean isIgnoringPlaySoundExceptedEntity(ServerPlayer player, ItemStack stack, InteractionHand hand, BlockState state, BlockHitResult blockHitResult, ServerLevel world) {
        if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.BLOCK, state.getBlock()) instanceof PolymerBlock polymerBlock && polymerBlock.isIgnoringBlockInteractionPlaySoundExceptedEntity(state, player, hand, stack, world, blockHitResult)) {
            return true;
        } else if (PolymerSyncedObject.getSyncedObject(BuiltInRegistries.ITEM, stack.getItem()) instanceof PolymerItem polymerItem && polymerItem.isIgnoringBlockInteractionPlaySoundExceptedEntity(state, player, hand, stack, world, blockHitResult)) {
            return true;
        }

        return POLYMER_IGNORE_SOUND_EXCEPTED_ENTITY.invoke(x -> x.isIgnoringBlockInteractionPlaySoundExceptedEntity(state, player, hand, stack, world, blockHitResult));
    }

    @FunctionalInterface
    public interface MineEventListener {
        boolean onBlockMine(BlockState state, BlockPos pos, ServerPlayer player);
    }

    @FunctionalInterface
    public interface BreakingProgressListener {
        void onBreakingProgressUpdate(ServerPlayer player, BlockPos pos, BlockState finalState, int i);
    }

    @FunctionalInterface
    public interface PolymerBlockInteractionListener {
        boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult);
    }

    @FunctionalInterface
    public interface PolymerIgnoreSoundExceptionListener {
        boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult);
    }
}
