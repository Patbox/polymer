package eu.pb4.polymer.interfaces;

import net.minecraft.util.math.BlockPos;

import java.util.Set;

public interface WorldChunkInterface {
    Set<BlockPos> getVirtualHeadBlocks();
}
