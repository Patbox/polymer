package eu.pb4.polymer.core.api.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

/**
 * Minimalistic implementation of PolymerBlock
*/
public class SimplePolymerBlock extends Block implements PolymerBlock {
    private final Block polymerBlock;

    public SimplePolymerBlock(Properties settings, Block polymerBlock) {
        super(settings);
        this.polymerBlock = polymerBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.polymerBlock.defaultBlockState();
    }
}
