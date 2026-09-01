package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;

@ApiStatus.Internal
public interface PolymerChunkStorage {
    @Nullable
    Iterator<BlockPos.MutableBlockPos> polymer$iterator();

    void polymer$setSynced(int x, int y, int z, boolean lightSource, boolean lightInside);
    void polymer$removeSynced(int x, int y, int z);

    boolean polymer$isSynced(int x, int y, int z);

    boolean polymer$hasAny();
}
