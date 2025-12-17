package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestMultiBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public TestMultiBlock(Properties settings, BlockModelType type, MultiPolymerBlockModel model) {
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
