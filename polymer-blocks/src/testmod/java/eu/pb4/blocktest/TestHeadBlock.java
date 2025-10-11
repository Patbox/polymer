package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestHeadBlock extends Block implements PolymerTexturedHeadBlock {
    private final BlockState polymerBlockState;

    public TestHeadBlock(Settings settings, BlockModelType type, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                PolymerBlockModel.of(Identifier.of("blocktest", modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }
}
