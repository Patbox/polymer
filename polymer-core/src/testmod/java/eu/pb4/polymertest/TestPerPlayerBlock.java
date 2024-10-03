package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.packettweaker.PacketContext;

public class TestPerPlayerBlock extends Block implements PolymerBlock {
    public TestPerPlayerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return context.getPlayer() != null && context.getPlayer().isCreative() ? Blocks.BEDROCK.getDefaultState() : Blocks.COBBLESTONE.getDefaultState();
    }
}
