package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.state.property.Properties;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestFluidBlock extends FluidBlock implements PolymerBlock {

    protected TestFluidBlock(FlowableFluid fluid, Settings settings) {
        super(fluid, settings);
    }
    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        var id = state.get(Properties.LEVEL_15);


        return (id % 2 == 0 ? Blocks.SOUL_FIRE : Blocks.FIRE).getDefaultState();
    }
}
