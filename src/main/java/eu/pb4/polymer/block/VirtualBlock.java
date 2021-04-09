package eu.pb4.polymer.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public interface VirtualBlock {
    Block getVirtualBlock();
    BlockState getDefaultVirtualBlockState();

    default BlockState getVirtualBlockState(BlockState state) {
        return this.getDefaultVirtualBlockState();
    };
}
