package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class SelfReferenceBlock extends Block implements PolymerBlock {
    public SelfReferenceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return /*this*/Blocks.STONE.getDefaultState();
    }
}
