package eu.pb4.polymer.impl.interfaces;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@ApiStatus.Internal
public interface PolymerBlockPosStorage {
    @Nullable
    ShortSet polymer_getBackendSet();

    @Nullable
    Iterator<BlockPos.Mutable> polymer_iterator(ChunkSectionPos sectionPos);

    @Nullable
    Iterator<BlockPos.Mutable> polymer_iterator();

    void polymer_setPolymer(int x, int y, int z);
    void polymer_removePolymer(int x, int y, int z);
    boolean polymer_getPolymer(int x, int y, int z);

    boolean polymer_hasAny();

    static short pack(int x, int y, int z) {
        return (short) ((x & 15) << 8 | (z & 15) << 4 | (y & 15) << 0);
    }

    record XYZ(byte x, int y, byte z) {};
}
