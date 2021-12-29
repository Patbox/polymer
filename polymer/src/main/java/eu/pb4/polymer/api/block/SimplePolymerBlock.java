package eu.pb4.polymer.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

/**
 * Minimalistic implementation of PolymerBlock
*/
public class SimplePolymerBlock extends Block implements PolymerBlock {
    private Block virtualBlock;

    public SimplePolymerBlock(Settings settings, Block virtualBlock) {
        super(settings);
        this.virtualBlock = virtualBlock;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.virtualBlock;
    }
}
