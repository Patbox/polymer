package eu.pb4.polymer.core.impl.interfaces;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@ApiStatus.Internal
public interface PolymerBlockPosStorage {
    @Nullable
    ShortSet polymer$getBackendSet();

    @Nullable
    Iterator<BlockPos.Mutable> polymer$iterator(ChunkSectionPos sectionPos);

    @Nullable
    Iterator<BlockPos.Mutable> polymer$iterator();

    void polymer$setSynced(int x, int y, int z);
    void polymer$removeSynced(int x, int y, int z);

    boolean polymer$isSynced(int x, int y, int z);

    boolean polymer$hasAny();

    static short pack(int x, int y, int z) {
        return (short) ((x & 15) << 8 | (z & 15) << 4 | (y & 15));
    }
}
