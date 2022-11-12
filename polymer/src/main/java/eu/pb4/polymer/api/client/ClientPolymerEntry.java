package eu.pb4.polymer.api.client;

import eu.pb4.polymer.impl.client.ClientPolymerEntryImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface ClientPolymerEntry<T> {
    Identifier identifier();
    @Nullable T registryEntry();

    static <T> ClientPolymerEntry<T> of(Identifier identifier, @Nullable T registryEntry) {
        return new ClientPolymerEntryImpl<>(identifier, registryEntry);
    }

    static <T> ClientPolymerEntry<T> of(Identifier identifier) {
        return new ClientPolymerEntryImpl<>(identifier, null);
    }
}
