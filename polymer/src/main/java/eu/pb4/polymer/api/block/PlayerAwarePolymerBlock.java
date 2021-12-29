package eu.pb4.polymer.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Predicate;

@ApiStatus.Experimental
public interface PlayerAwarePolymerBlock extends PolymerBlock {
    Predicate<BlockState> PLAYER_AWARE_BLOCK_STATE_PREDICATE = o -> o.getBlock() instanceof PlayerAwarePolymerBlock;

    /**
     * Returns block used on client for player
     *
     * @return Vanilla (or other) Block instance
     */
    default Block getPolymerBlock(ServerPlayerEntity player, BlockState state) {
        return this.getPolymerBlock(state);
    }

    /**
     * Allows to change blockState per player
     * Keep in mind you should ideally use blocks with the same hitbox as normal ones!
     *
     * @param player Player viewing it
     * @param state Server side BlocksState
     * @return Client side BlockState
     */
    default BlockState getPolymerBlockState(ServerPlayerEntity player, BlockState state) {
        return this.getPolymerBlockState(state);
    }

}
