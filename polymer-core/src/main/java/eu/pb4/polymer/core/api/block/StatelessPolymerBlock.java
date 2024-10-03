package eu.pb4.polymer.core.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

public interface StatelessPolymerBlock extends PolymerBlock {
    /**
     * Returns block used on client for player
     *
     * @return Vanilla (or other) Block instance
     */
    Block getPolymerBlock(BlockState state, PacketContext context);

    @Override
    default BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.getPolymerBlock(state, context).getDefaultState();
    }
}
