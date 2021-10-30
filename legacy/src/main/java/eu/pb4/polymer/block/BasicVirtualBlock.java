package eu.pb4.polymer.block;

import net.minecraft.block.Block;

/**
 * Use {@link eu.pb4.polymer.api.block.SimplePolymerBlock} instead
 */
@Deprecated
public class BasicVirtualBlock extends Block implements VirtualBlock {
    private Block virtualBlock;

    public BasicVirtualBlock(Settings settings, Block virtualBlock) {
        super(settings);
        this.virtualBlock = virtualBlock;
    }

    @Override
    public Block getVirtualBlock() {
        return this.virtualBlock;
    }
}
