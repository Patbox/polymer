package eu.pb4.polymer.rsm.api;

import eu.pb4.polymer.rsm.impl.QuiltRegistryUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public final class RegistrySyncUtils {
    private RegistrySyncUtils() {}

    public static <T> boolean isServerEntry(Registry<T> registry, T entry) {
        if (QuiltRegistryUtils.isOptional(registry, entry)) {
            return true;
        }

        if (registry instanceof RegistrySyncExtension<?>) {
            return ((RegistrySyncExtension<T>) registry).polymer_registry_sync$isServerEntry(entry);
        } else {
            return false;
        }
    }

    public static <T> boolean isServerEntry(Registry<T> registry, Identifier identifier) {
        return registry.containsKey(identifier) ? isServerEntry(registry, registry.getValue(identifier)) : false;
    }

    public static <T> void setServerEntry(Registry<T> registry, T entry) {
        if (registry instanceof RegistrySyncExtension<?>) {
            ((RegistrySyncExtension<T>) registry).polymer_registry_sync$setServerEntry(entry, true);
            QuiltRegistryUtils.markAsOptional(registry, entry);
        }
    }

    public static <T> void setServerEntry(Registry<T> registry, Identifier identifier) {
        if (registry.containsKey(identifier)) {
            setServerEntry(registry, registry.getValue(identifier));
        } else {
            throw new IllegalArgumentException("Entry '" + identifier + "' of registry '" + registry.key().identifier() + "' isn't registered!");
        }
    }
}
