package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.block.BlockMapper;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ChunkDataS2CPacketInterface {
    WorldChunk polymer$getWorldChunk();
    BlockMapper polymer$getMapper();
    boolean polymer$hasPlayerDependentBlocks();
}
