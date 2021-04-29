package eu.pb4.polymertest;

import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.block.VirtualHeadBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.util.math.Direction;

public class TestBlock extends Block implements VirtualBlock {
    public TestBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getVirtualBlock() {
        return Blocks.DISPENSER;
    }

    @Override
    public BlockState getDefaultVirtualBlockState() {
        return Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, Direction.UP);
    }
}
