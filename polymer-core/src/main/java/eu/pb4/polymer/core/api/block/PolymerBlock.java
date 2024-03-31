package eu.pb4.polymer.core.api.block;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Interface used for creation of server side blocks
 */
public interface PolymerBlock extends PolymerSyncedObject<Block> {

    /**
     * Generic method used for replacing BlockStates in case of no player context
     * It also controls some server side things like collisions
     *
     * @param state Server side/real BlockState
     * @return BlockState visible on client
     */
    BlockState getPolymerBlockState(BlockState state);

    /**
     * Main method used for replacing BlockStates for players
     * Keep in mind you should ideally use blocks with the same hitbox as generic/non-player ones!
     *
     * @param state Server side BlocksState
     * @param player Player viewing it
     * @return Client side BlockState
     */
    default BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        return this.getPolymerBlockState(state);
    }

    /**
     * This method is called when block gets send to player
     * Allows to add client-only BlockEntities (for signs, heads, etc)
     *
     * @param blockState Real BlockState of block
     * @param pos Position of block. Keep in mind it's mutable,
 *            so make sure to use {@link BlockPos.Mutable#toImmutable()}
 *            in case of using in packets, as it's reused for other positions!
     * @param player Player packets should be send to
     */
    default void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player) { }

    /**
     * You can override this method in case of issues with light updates of this block. In most cases it's not needed.
     * @param blockState
     */
    default boolean forceLightUpdates(BlockState blockState) { return false; }

    /**
     * Overrides breaking particle used by the block
     * @param state
     * @param player
     * @return
     */
    default BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return state;
    }

    @Override
    default Block getPolymerReplacement(ServerPlayerEntity player) {
        return PolymerBlockUtils.getPolymerBlock((Block) this, player);
    }

    default boolean handleMiningOnServer(ItemStack tool, BlockState state, BlockPos pos, ServerPlayerEntity player) {
        return true;
    }
}
