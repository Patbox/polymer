package eu.pb4.polymer.core.impl.interfaces;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

@ApiStatus.Internal
public interface PolymerChunkSectionStorage {
    ShortIterator polymer$getRawBlockIterator();

    Iterator<BlockPos.MutableBlockPos> polymer$blockIterator(SectionPos sectionPos);

    Iterator<BlockPos.MutableBlockPos> polymer$lightInsideIterator(SectionPos sectionPos);

    void polymer$setSynced(int x, int y, int z, boolean lightSource, boolean lightInside);
    void polymer$removeSynced(int x, int y, int z);

    boolean polymer$isSynced(int x, int y, int z);

    boolean polymer$hasAny();

    static int pack(int x, int y, int z) {
        return ((x & 15) << 8 | (z & 15) << 4 | (y & 15)) & 0xFFFF;
    }
    boolean polymer$hasLights();
    boolean polymer$requireLights();
    void polymer$setRequireLights(boolean value);
}
