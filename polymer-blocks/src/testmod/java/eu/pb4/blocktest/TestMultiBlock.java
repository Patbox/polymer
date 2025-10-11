package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestMultiBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public TestMultiBlock(Settings settings, BlockModelType type, MultiPolymerBlockModel model) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                model);

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }
}
