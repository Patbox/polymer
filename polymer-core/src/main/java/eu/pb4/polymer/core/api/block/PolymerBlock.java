package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

/**
 * Interface used for creation of server side blocks
 */
public interface PolymerBlock extends PolymerSyncedObject<Block> {
    /**
     * Main method used for replacing BlockStates for players
     * Keep in mind you should ideally use blocks with the same hitbox as generic/non-player ones!
     *
     * @param state Server side BlocksState
     * @param context PacketContext this method is called with, might be null!
     * @return Client side BlockState
     */
    BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context);

    /**
     * This method is called when block gets send to player
     * Allows to add client-only BlockEntities (for signs, heads, etc)
     *
     * @param blockState Real BlockState of block
     * @param pos        Position of block. Keep in mind it's mutable,
     *                   so make sure to use {@link BlockPos.MutableBlockPos#immutable()}
     *                   in case of using in packets, as it's reused for other positions!
     * @param player     Context packet is sent to. Should always contain a player
     */
    default void onPolymerBlockSend(BlockState blockState, BlockPos.MutableBlockPos pos, ServerPlayer player) { }

    /**
     * You can override this method in case of issues with light updates of this block. In most cases it's not needed.
     * @param blockState
     */
    default boolean forceLightUpdates(BlockState blockState) { return false; }

    /**
     * You can override this method to force light to be estimated for the block position. This is useful for blocks
     * using display entities, which sample their brightness from the light level at their position
     * @param blockState
     */
    default boolean forceLightInsideBlock(BlockState blockState) { return false; }

    /**
     * Overrides breaking particle used by the block
     * @param state
     * @param context
     * @return
     */
    default BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return state;
    }

    @Override
    default Block getPolymerReplacement(Block block, PacketContext context) {
        return PolymerBlockUtils.getPolymerBlock(block, context);
    }

    default boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayer player) {
        return true;
    }

    default boolean isPolymerBlockInteraction(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult, InteractionResult actionResult) {
        return true;
    }

    default boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return false;
    }

    default boolean playSoundToSelf(BlockState state, ServerPlayer player, ServerLevel world, BlockPos pos) {
        return false;
    }

    default boolean overridePlayerCollisionsWithPolymer(BlockGetter level, BlockPos pos, BlockState blockState, ServerPlayer player) {
        return true;
    }

    static void registerOverlay(Block block, PolymerBlock polymerBlock) {
        PolymerSyncedObject.setSyncedObject(BuiltInRegistries.BLOCK, block, polymerBlock);
        RegistrySyncUtils.setServerEntry(BuiltInRegistries.BLOCK, block);
    }
}
