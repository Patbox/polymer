package eu.pb4.polymertest;

import eu.pb4.polymer.api.block.PlayerAwarePolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;

public class TestPerPlayerBlock extends Block implements PlayerAwarePolymerBlock {
    public TestPerPlayerBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.DISPENSER;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBlockState(ServerPlayerEntity player, BlockState state) {
        return player.isCreative() ? Blocks.BEDROCK.getDefaultState() : Blocks.COBBLESTONE.getDefaultState();
    }
}
