package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TestBlock extends Block implements PolymerBlock {

    public TestBlock(Settings settings) {
        super(settings);
    }

    private static final IntProperty TEST = IntProperty.of("test", 0, 1000);

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(TEST, ctx.getPlayer().getStackInHand(ctx.getHand()).getOrCreateNbt().getInt("y"));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        itemStack.getOrCreateNbt().putInt("y", itemStack.getOrCreateNbt().getInt("y") + 1);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.DISPENSER;
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(TEST);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, Direction.UP);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, ServerPlayerEntity player) {
        return Blocks.GRASS.getDefaultState();
    }
}
