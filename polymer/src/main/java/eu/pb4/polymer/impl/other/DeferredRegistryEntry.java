package eu.pb4.polymer.impl.other;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.RegistryKey;

public record DeferredRegistryEntry<T>(RegistryKey<T> registryKey, T entry, Lifecycle lifecycle) {
}
