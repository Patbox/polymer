package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.state.property.Properties;

public class TestFluidBlock extends FluidBlock implements PolymerBlock {

    protected TestFluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.WATER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        var id = state.get(Properties.LEVEL_15);

        return (id % 2 == 0 ? Blocks.WATER : Blocks.LAVA).getDefaultState().with(Properties.LEVEL_15, id);
    }
}
