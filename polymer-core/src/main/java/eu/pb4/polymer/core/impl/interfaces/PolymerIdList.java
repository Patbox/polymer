package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface PolymerIdList {
    void polymer$enableLazyBlockStates();
    void polymer$setIgnoreCalls(boolean value);
    Collection<BlockState> polymer$getPolymerStates();
    int polymer$getOffset();
    void polymer$clear();

    void polymer$setReorderLock(boolean value);
    boolean polymer$getReorderLock();
}
