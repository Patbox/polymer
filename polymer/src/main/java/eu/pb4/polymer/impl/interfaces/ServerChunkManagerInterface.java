package eu.pb4.polymer.impl.interfaces;

import net.minecraft.util.math.ChunkSectionPos;

public interface ServerChunkManagerInterface {
    void polymer_setSection(ChunkSectionPos pos, boolean hasPolymer);
    void polymer_removeSection(ChunkSectionPos pos);
}
