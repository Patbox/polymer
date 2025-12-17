package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerClientDecoded;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestClientBlock extends Block implements PolymerBlock, PolymerKeepModel, PolymerClientDecoded {
    public TestClientBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.UP);
    }
}
