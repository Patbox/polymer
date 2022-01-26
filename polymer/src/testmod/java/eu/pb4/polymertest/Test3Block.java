package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class Test3Block extends Block implements PolymerBlock {
    public Test3Block(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.AIR;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.COBWEB.getDefaultState();
    }
}
