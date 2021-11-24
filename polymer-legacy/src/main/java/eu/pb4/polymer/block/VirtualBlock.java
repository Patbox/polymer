package eu.pb4.polymer.block;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/**
 * Use {@link eu.pb4.polymer.api.block.PolymerBlock} instead
 */
@Deprecated
public interface VirtualBlock extends PolymerBlock, VirtualObject {
    Block getVirtualBlock();

    @Override
    default Block getPolymerBlock(BlockState state) {
        return this.getVirtualBlock();
    }

    default Block getVirtualBlock(BlockPos pos, World world) {
        return this.getVirtualBlock();
    }

    @Deprecated
    default BlockState getDefaultVirtualBlockState() {
        return this.getVirtualBlock().getDefaultState();
    }

    default BlockState getVirtualBlockState(BlockState state) {
        return this.getDefaultVirtualBlockState();
    }

    @Override
    default BlockState getPolymerBlockState(BlockState state) {
        return getVirtualBlockState(state);
    }

    default void sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState) {}

    @Override
    default void onPolymerBlockSend(ServerPlayerEntity player, BlockPos.Mutable pos, BlockState blockState) {
        this.sendPacketsAfterCreation(player, pos.toImmutable(), blockState);
    }

    default boolean forceLightUpdates() {
        return false;
    }
}
