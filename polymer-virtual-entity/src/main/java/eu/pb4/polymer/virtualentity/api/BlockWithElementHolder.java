package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.virtualentity.impl.BlockExt;
import eu.pb4.polymer.virtualentity.impl.VirtualEntityMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Allows for automatic creation of element holders bound to blocks.
 * Can be used by either implementing this interface on top of custom Block class,
 * or by calling BlockWithElementHolder#registerOverlay to register it as overlay.
 *
 * Block can have only single controlling BlockWithElementHolder
 */
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

    @Nullable
    static BlockWithElementHolder get(BlockState state) {
        return ((BlockExt) state.getBlock()).polymerVE$getElementHolderCreator();
    }

    static boolean registerOverlay(Block block, BlockWithElementHolder holder) {
        return ((BlockExt) block).polymerVE$setElementHolderCreator(holder);
    }
}
