package eu.pb4.polymer.virtualentity.impl;

import it.unimi.dsi.fastutil.ints.IntList;

public interface EntityExt {
    IntList polymerVE$getVirtualRidden();
    void polymerVE$markVirtualRiddenDirty();
    boolean polymerVE$getAndClearVirtualRiddenDirty();
}
