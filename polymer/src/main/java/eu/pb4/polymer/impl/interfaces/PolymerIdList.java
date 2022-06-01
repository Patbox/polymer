package eu.pb4.polymer.impl.interfaces;

import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;

@ApiStatus.Internal
public interface PolymerIdList {
    void polymer_enableLazyBlockStates();
    void polymer_setIgnoreCalls(boolean value);
    Collection<BlockState> polymer_getPolymerStates();
    int polymer_getOffset();
    void polymer_clear();
}
