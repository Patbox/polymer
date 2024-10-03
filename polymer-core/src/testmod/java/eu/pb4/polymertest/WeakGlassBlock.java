package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TranslucentBlock;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class WeakGlassBlock extends TranslucentBlock implements PolymerBlock {
    public static final int DAMAGE_STATES = 4;
    public static final IntProperty DAMAGE = IntProperty.of("damage", 0, DAMAGE_STATES);

    public WeakGlassBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(DAMAGE);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
    return switch (state.get(DAMAGE)) {
            case 0 -> Blocks.GLASS.getDefaultState();
            case 1 -> Blocks.WHITE_STAINED_GLASS.getDefaultState();
            default -> Blocks.BEDROCK.getDefaultState();
        };
    }

    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        int damage = state.get(DAMAGE);
        if (damage == 1) {
            world.breakBlock(hit.getBlockPos(), false);
        } else {
            world.setBlockState(hit.getBlockPos(), state.with(DAMAGE, Math.min(damage + 1, DAMAGE_STATES)));
        }
        super.onProjectileHit(world, state, hit, projectile);
    }
}
