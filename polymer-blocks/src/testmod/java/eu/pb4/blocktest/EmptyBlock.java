package eu.pb4.blocktest;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

public class EmptyBlock extends Block implements PolymerTexturedBlock {
    private final BlockState polymerBlockState;

    public EmptyBlock(Properties settings, BlockModelType type) {
        super(settings);
        this.polymerBlockState = PolymerBlockResourceUtils.requestEmpty(type);

    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return this.polymerBlockState;
    }
}
