package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.impl.other.DualList;
import net.minecraft.block.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface NetworkIdList {
    void polymer_enableOffset();
    DualList<BlockState> polymer_getInternalList();

    void polymer_clear();
}
