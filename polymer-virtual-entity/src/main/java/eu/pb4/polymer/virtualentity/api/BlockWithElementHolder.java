package eu.pb4.polymer.virtualentity.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface BlockWithElementHolder {
    ElementHolder createElementHolder(BlockPos pos, BlockState initialBlockState);

    default Vec3d getElementHolderOffset() {
        return Vec3d.ZERO;
    }

    default boolean tickElementHolder() {
        return false;
    }
}
