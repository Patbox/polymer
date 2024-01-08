package eu.pb4.polymer.virtualentity.api;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface BlockWithMovingElementHolder extends BlockWithElementHolder {

    @Override
    @Nullable
    default ElementHolder createMovingElementHolder(ServerWorld world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldStaticElementHolder) {
        return oldStaticElementHolder != null ? oldStaticElementHolder : createElementHolder(world, blockPos, blockState);
    }
    @Override
    @Nullable
    default ElementHolder createStaticElementHolder(ServerWorld world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldMovingElementHolder) {
        return oldMovingElementHolder != null ? oldMovingElementHolder : createElementHolder(world, blockPos, blockState);
    }
}
