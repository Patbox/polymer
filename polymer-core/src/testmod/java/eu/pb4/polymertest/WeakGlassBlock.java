package eu.pb4.polymertest;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import org.jspecify.annotations.Nullable;

public class WeakGlassBlock extends HalfTransparentBlock implements PolymerBlock {
    public static final int DAMAGE_STATES = 4;
    public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, DAMAGE_STATES);

    public WeakGlassBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DAMAGE);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
    return switch (state.getValue(DAMAGE)) {
            case 0 -> Blocks.GLASS.defaultBlockState();
            case 1 -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            default -> Blocks.BEDROCK.defaultBlockState();
        };
    }

    @Override
    public void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
        int damage = state.getValue(DAMAGE);
        if (damage == 1) {
            world.destroyBlock(hit.getBlockPos(), false);
        } else {
            world.setBlockAndUpdate(hit.getBlockPos(), state.setValue(DAMAGE, Math.min(damage + 1, DAMAGE_STATES)));
        }
        super.onProjectileHit(world, state, hit, projectile);
    }
}
