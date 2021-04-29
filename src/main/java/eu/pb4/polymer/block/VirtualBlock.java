package eu.pb4.polymer.block;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface VirtualBlock extends VirtualObject {
    Block getVirtualBlock();
    default BlockState getDefaultVirtualBlockState() {
        return this.getVirtualBlock().getDefaultState();
    }

    default BlockState getVirtualBlockState(BlockState state) {
        return this.getDefaultVirtualBlockState();
    };

    default void sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState) {}
}
