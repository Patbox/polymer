package eu.pb4.polymer.block;

import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


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
     * Returns block used on client for provided location;
     * It's used to validate blocks
     *
     * @return Vanilla (or other) Block instance
     */
    default Block getVirtualBlock(BlockPos pos, World world) {
        return this.getVirtualBlock();
    }


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
     * It also controls some server side things like collisions
     *
     * @param state Server side/real BlockState
     * @return BlockState visible on client
     */
    default BlockState getVirtualBlockState(BlockState state) {
        return this.getDefaultVirtualBlockState();
    }

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
