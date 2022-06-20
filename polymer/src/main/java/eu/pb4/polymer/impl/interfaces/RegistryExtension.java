package eu.pb4.polymer.impl.interfaces;

import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;

import java.util.List;
import java.util.Map;

public interface RegistryExtension<T> {
    static <T> List<T> getPolymerEntries(Registry<T> registry) {
        return ((RegistryExtension<T>) registry).polymer_getEntries();
    }


    Map<TagKey<T>, RegistryEntryList.Named<T>> polymer_getTagsInternal();
    List<T> polymer_getEntries();
}
