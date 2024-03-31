package eu.pb4.polymer.core.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;

public interface StatelessPolymerBlock extends PolymerBlock {
    /**
     * Returns main/default block used on client
     *
     * @return Vanilla (or other) Block instance
     */
    Block getPolymerBlock(BlockState state);

    /**
     * Returns block used on client for player
     *
     * @return Vanilla (or other) Block instance
     */
    default Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return this.getPolymerBlock(state, player);
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state) {
        return this.getPolymerBlock(state).getDefaultState();
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        return this.getPolymerBlock(state, player).getDefaultState();
    }
}
