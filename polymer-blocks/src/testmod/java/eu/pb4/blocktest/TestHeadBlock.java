package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestHeadBlock extends Block implements PolymerTexturedHeadBlock {
    private final BlockState polymerBlockState;

    public TestHeadBlock(Properties settings, BlockModelType type, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                PolymerBlockModel.of(Identifier.fromNamespaceAndPath("blocktest", modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }
}
