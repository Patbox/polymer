package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerEntityProvider {
    @Nullable
    PolymerEntity polymer$getPolymerEntity();
    void polymer$recreatePolymerEntity();

    void polymer$setPolymerEntity(PolymerEntity polymerEntity);
}
