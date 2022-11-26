package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

import java.util.List;
import java.util.Map;

public interface RegistryExtension<T> {
    static <T> List<T> getPolymerEntries(Registry<T> registry) {
        return ((RegistryExtension<T>) registry).polymer$getEntries();
    }


    Map<TagKey<T>, RegistryEntryList.Named<T>> polymer$getTagsInternal();
    List<T> polymer$getEntries();
}
