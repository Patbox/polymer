package eu.pb4.polymer.core.api.client;

import eu.pb4.polymer.core.impl.client.ClientPolymerEntryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ClientPolymerEntry<T> {
    Identifier identifier();
    @Nullable T registryEntry();

    static <T> ClientPolymerEntry<T> of(Identifier identifier, @Nullable T registryEntry) {
        return new ClientPolymerEntryImpl<>(identifier, registryEntry);
    }

    static <T> ClientPolymerEntry<T> of(Identifier identifier, Registry<T> registry) {
        return new ClientPolymerEntryImpl<>(identifier, registry.containsId(identifier) ? registry.get(identifier) : null);
    }

    static <T> ClientPolymerEntry<T> of(Identifier identifier) {
        return new ClientPolymerEntryImpl<>(identifier, null);
    }
}
