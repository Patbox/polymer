package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

public class TestHeadBlock extends Block implements PolymerTexturedHeadBlock {
    private final BlockState polymerBlockState;

    public TestHeadBlock(Properties settings, BlockModelType type, String modelId) {
        super(settings);

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
                type,
                PolymerBlockModel.of(Identifier.fromNamespaceAndPath("blocktest", modelId)));

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.polymerBlockState;
    }
}
