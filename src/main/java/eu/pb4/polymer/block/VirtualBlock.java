package eu.pb4.polymer.block;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;


/**
 * Interface used for creation of server side blocks
 */
public interface VirtualBlock extends VirtualObject {
    /**
     * Returns main/default block used on client
     *
     * @return Vanilla (or other) Block instance
     */
    Block getVirtualBlock();

    /**
     * Returns default virtual BlockState
     *
     * @return Default BlockState of Vanilla/other block
     */
    default BlockState getDefaultVirtualBlockState() {
        return this.getVirtualBlock().getDefaultState();
    }

    /**
     * Main method used for replacing BlockStates on client
     *
     * @param state Server side/real BlockState
     * @return BlockState visible on client
     */
    default BlockState getVirtualBlockState(BlockState state) {
        return this.getDefaultVirtualBlockState();
    };

    /**
     * This method can be used to send additional packets after block is send to client
     * Allows to add client-only BlockEntities (for signs, heads, etc)
     *
     * @param player Player packets should be send to
     * @param pos Position of block
     * @param blockState Real BlockState of block
     */
    default void sendPacketsAfterCreation(ServerPlayerEntity player, BlockPos pos, BlockState blockState) {}
}
