package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;

public interface RegistryExtension<T> {
    static <T> List<T> getPolymerEntries(Registry<T> registry) {
        return ((RegistryExtension<T>) registry).polymer$getEntries();
    }

    Map<TagKey<T>, HolderSet.Named<T>> polymer$getTagsInternal();
    List<T> polymer$getEntries();

    void polymer$setOverlay(T value, PolymerSyncedObject<T> syncedObject);
    PolymerSyncedObject<T> polymer$getOverlay(T value);
}
