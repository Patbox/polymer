package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class Test3Block extends Block implements PolymerBlock {
    public Test3Block(Properties settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state , PacketContext context) {
        return Blocks.COBWEB.defaultBlockState();
    }
}
