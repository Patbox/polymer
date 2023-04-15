package eu.pb4.polymer.virtualentity.api;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockWithElementHolder {
    default ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return createElementHolder(pos, initialBlockState);
    }

    default Vec3d getElementHolderOffset(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return getElementHolderOffset();
    }

    default boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return tickElementHolder();
    }

    @Deprecated
    default Vec3d getElementHolderOffset() {
        return Vec3d.ZERO;
    }

    @Deprecated
    default ElementHolder createElementHolder(BlockPos pos, BlockState initialBlockState) {
        return null;
    };

    @Deprecated
    default boolean tickElementHolder() {
        return false;
    }
}
