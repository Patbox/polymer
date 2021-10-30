package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class SelfReferenceBlock extends Block implements PolymerBlock {
    public SelfReferenceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this;
    }
}
