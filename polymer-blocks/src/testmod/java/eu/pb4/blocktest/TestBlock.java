package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public TestBlock(Properties settings, BlockModelType type, String modelId) {
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
