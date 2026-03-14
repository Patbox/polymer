package eu.pb4.polymer.virtualentity.api;

import eu.pb4.polymer.virtualentity.impl.BlockExt;
import eu.pb4.polymer.virtualentity.impl.VirtualEntityMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Allows for automatic creation of element holders bound to blocks.
 * Can be used by either implementing this interface on top of custom Block class,
 * or by calling BlockWithElementHolder#registerOverlay to register it as overlay.
 *
 * Block can have only single controlling BlockWithElementHolder
 */
public interface BlockWithElementHolder {
    @Nullable
    default ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return null;
    }

    default Vec3 getElementHolderOffset(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return Vec3.ZERO;
    }

    default boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return false;
    }

    @Nullable
    default ElementHolder createMovingElementHolder(ServerLevel world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldStaticElementHolder) {
        return oldStaticElementHolder != null ? oldStaticElementHolder : createElementHolder(world, blockPos, blockState);
    }

    @Nullable
    default ElementHolder createStaticElementHolder(ServerLevel world, BlockPos blockPos, BlockState blockState, @Nullable ElementHolder oldMovingElementHolder) {
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
