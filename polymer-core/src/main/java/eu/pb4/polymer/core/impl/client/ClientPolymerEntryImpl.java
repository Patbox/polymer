package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.core.api.client.ClientPolymerEntry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record ClientPolymerEntryImpl<T>(Identifier identifier, @Nullable T registryEntry) implements ClientPolymerEntry<T> {
}
