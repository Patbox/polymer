package eu.pb4.polymer.virtualentity.api;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public interface BlockWithElementHolder {
    @Nullable
    default ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return null;
    }

    default Vec3d getElementHolderOffset(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return Vec3d.ZERO;
    }

    default boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return false;
    }

    @Nullable
    default ElementHolder createMovingElementHolder(ServerWorld world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldStaticElementHolder) {
        return oldStaticElementHolder != null ? oldStaticElementHolder : createElementHolder(world, blockPos, blockState);
    }

    @Nullable
    default ElementHolder createStaticElementHolder(ServerWorld world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldMovingElementHolder) {
        return oldMovingElementHolder != null ? oldMovingElementHolder : createElementHolder(world, blockPos, blockState);
    }
}
