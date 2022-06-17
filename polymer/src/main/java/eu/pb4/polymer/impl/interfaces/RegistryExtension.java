package eu.pb4.polymer.impl.interfaces;

import net.minecraft.util.registry.Registry;

import java.util.List;

public interface RegistryExtension<T> {
    static <T> List<T> getPolymerEntries(Registry<T> registry) {
        return ((RegistryExtension<T>) registry).polymer_getEntries();
    }

    List<T> polymer_getEntries();
}
