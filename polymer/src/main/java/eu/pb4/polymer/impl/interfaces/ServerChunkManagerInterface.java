package eu.pb4.polymer.impl.interfaces;

import net.minecraft.util.math.ChunkSectionPos;

public interface ServerChunkManagerInterface {
    void polymer$setSection(ChunkSectionPos pos, boolean hasPolymer);
    void polymer$removeSection(ChunkSectionPos pos);
}
