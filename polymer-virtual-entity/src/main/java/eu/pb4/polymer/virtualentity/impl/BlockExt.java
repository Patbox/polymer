package eu.pb4.polymer.virtualentity.impl;

import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import org.jetbrains.annotations.Nullable;

public interface BlockExt {
    boolean polymerVE$setElementHolderCreator(BlockWithElementHolder holder);
    @Nullable
    BlockWithElementHolder polymerVE$getElementHolderCreator();
}
