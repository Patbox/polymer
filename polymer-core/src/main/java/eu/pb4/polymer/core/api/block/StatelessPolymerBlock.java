package eu.pb4.polymer.core.api.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

public interface StatelessPolymerBlock extends PolymerBlock {
    /**
     * Returns block used on client for player
     *
     * @return Vanilla (or other) Block instance
     */
    Block getPolymerBlock(BlockState state, PacketContext context);

    @Override
    default BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.getPolymerBlock(state, context).defaultBlockState();
    }
}
