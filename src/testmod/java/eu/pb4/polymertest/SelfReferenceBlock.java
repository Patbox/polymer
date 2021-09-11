package eu.pb4.polymertest;

import eu.pb4.polymer.block.VirtualBlock;
import net.minecraft.block.Block;

public class SelfReferenceBlock extends Block implements VirtualBlock {
    public SelfReferenceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getVirtualBlock() {
        return this;
    }
}
