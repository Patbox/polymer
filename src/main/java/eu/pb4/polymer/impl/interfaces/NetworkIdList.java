package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.impl.other.DualList;
import net.minecraft.block.BlockState;

public interface NetworkIdList {
    void polymer_enableOffset();
    DualList<BlockState> polymer_getInternalList();
}
