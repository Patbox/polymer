package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Interface used for creation of server side blocks
 */
public interface PolymerBlock extends PolymerSyncedObject<Block> {
    /**
     * Main method used for replacing BlockStates for players
     * Keep in mind you should ideally use blocks with the same hitbox as generic/non-player ones!
     *
     * @param state Server side BlocksState
     * @param context PacketContext this method is called with, might be empty!
     * @return Client side BlockState
     */
    BlockState getPolymerBlockState(BlockState state, PacketContext context);

    /**
     * This method is called when block gets send to player
     * Allows to add client-only BlockEntities (for signs, heads, etc)
     *
     * @param blockState Real BlockState of block
     * @param pos Position of block. Keep in mind it's mutable,
 *            so make sure to use {@link BlockPos.Mutable#toImmutable()}
 *            in case of using in packets, as it's reused for other positions!
     * @param contexts Context packet is sent to. Should always contain a player
     */
    default void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, PacketContext.NotNullWithPlayer contexts) { }

    /**
     * You can override this method in case of issues with light updates of this block. In most cases it's not needed.
     * @param blockState
     */
    default boolean forceLightUpdates(BlockState blockState) { return false; }

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
    default Block getPolymerReplacement(PacketContext context) {
        return PolymerBlockUtils.getPolymerBlock((Block) this, context);
    }

    default boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return true;
    }

    default boolean isPolymerBlockInteraction(BlockState state, ServerPlayerEntity player, Hand hand, ItemStack stack, ServerWorld world, BlockHitResult blockHitResult, ActionResult actionResult) {
        return true;
    }
}
