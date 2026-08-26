package eu.pb4.polymer.core.impl.interfaces;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.Iterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

@ApiStatus.Internal
public interface PolymerChunkSectionStorage {
    ShortSet polymer$getBackendSet();

    Iterator<BlockPos.MutableBlockPos> polymer$blockIterator(SectionPos sectionPos);

    Iterator<BlockPos.MutableBlockPos> polymer$lightInsideIterator(SectionPos sectionPos);

    void polymer$setSynced(int x, int y, int z, boolean lightSource, boolean lightInside);
    void polymer$removeSynced(int x, int y, int z);

    boolean polymer$isSynced(int x, int y, int z);

    boolean polymer$hasAny();

    static short pack(int x, int y, int z) {
        return (short) ((x & 15) << 8 | (z & 15) << 4 | (y & 15));
    }
    boolean polymer$hasLights();
    boolean polymer$requireLights();
    void polymer$setRequireLights(boolean value);
}
