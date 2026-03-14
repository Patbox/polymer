package eu.pb4.polymertest;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

public class TestPerPlayerBlock extends Block implements PolymerBlock {
    public TestPerPlayerBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return context != null && PolymerCommonUtils.getPlayer(context) != null && PolymerCommonUtils.getPlayer(context).isCreative() ? Blocks.BEDROCK.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
    }
}
