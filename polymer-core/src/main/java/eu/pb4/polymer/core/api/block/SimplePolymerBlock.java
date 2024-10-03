package eu.pb4.polymer.core.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

/**
 * Minimalistic implementation of PolymerBlock
*/
public class SimplePolymerBlock extends Block implements PolymerBlock {
    private final Block polymerBlock;

    public SimplePolymerBlock(Settings settings, Block polymerBlock) {
        super(settings);
        this.polymerBlock = polymerBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlock.getDefaultState();
    }
}
