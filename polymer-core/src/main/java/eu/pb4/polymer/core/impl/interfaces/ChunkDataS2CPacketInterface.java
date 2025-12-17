package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ChunkDataS2CPacketInterface {
    LevelChunk polymer$getWorldChunk();
}
