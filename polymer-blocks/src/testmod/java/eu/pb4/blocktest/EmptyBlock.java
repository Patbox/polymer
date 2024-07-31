package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class EmptyBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public EmptyBlock(Settings settings, BlockModelType type) {
        super(settings);
        this.polymerBlockState = PolymerBlockResourceUtils.requestEmpty(type);

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.polymerBlockState;
    }
}
