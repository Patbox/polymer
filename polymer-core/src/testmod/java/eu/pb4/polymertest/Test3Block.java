package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import xyz.nucleoid.packettweaker.PacketContext;

public class Test3Block extends Block implements PolymerBlock {
    public Test3Block(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state , PacketContext context) {
        return Blocks.COBWEB.getDefaultState();
    }
}
