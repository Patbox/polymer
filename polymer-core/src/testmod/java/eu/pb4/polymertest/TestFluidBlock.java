package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestFluidBlock extends LiquidBlock implements PolymerBlock {

    protected TestFluidBlock(FlowingFluid fluid, Properties settings) {
        super(fluid, settings);
    }
    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        var id = state.getValue(BlockStateProperties.LEVEL);


        return (id % 2 == 0 ? Blocks.SOUL_FIRE : Blocks.FIRE).defaultBlockState();
    }
}
