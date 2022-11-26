package eu.pb4.polymer.core.impl.client;

import eu.pb4.polymer.core.api.client.ClientPolymerEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record ClientPolymerEntryImpl<T>(Identifier identifier, @Nullable T registryEntry) implements ClientPolymerEntry<T> {
}
