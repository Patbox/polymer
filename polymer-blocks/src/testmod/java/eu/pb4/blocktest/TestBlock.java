package eu.pb4.blocktest;

import eu.pb4.polymer.ext.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.ext.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public TestBlock(Settings settings, PolymerBlockResourceUtils.Type type, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                PolymerBlockModel.of(new Identifier("blocktest", modelId)));

    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return this.polymerBlockState.getBlock();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return this.polymerBlockState;
    }
}
