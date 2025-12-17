package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class SelfReferenceBlock extends Block implements PolymerBlock {
    public SelfReferenceBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return /*this*/Blocks.STONE.defaultBlockState();
    }
}
