package eu.pb4.polymer.impl.compat;

import eu.pb4.polymer.impl.PolymerImpl;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

@ApiStatus.Internal
public final class QuiltRegistryUtils {
    private static Method markAsOptional = null;
    private static boolean triedOnce = false;

    public static void markAsOptional(Registry<?> registry, Object entry) {
        try {
            if (markAsOptional == null && !triedOnce) {
                triedOnce = true;
                markAsOptional = Class.forName("org.quiltmc.qsl.registry.api.sync.RegistrySynchronization").getMethod("setEntryOptional", SimpleRegistry.class, Object.class);
            }

            if (markAsOptional != null) {
                markAsOptional.invoke(null, registry, entry);
            }
        } catch (Throwable t) {
            PolymerImpl.LOGGER.warn("Quilt Registry module is present, but there is no RegistrySynchronization#setEntryOptional(SimpleRegistry<T>, T). Error: {}", t.getMessage());
        }
    }
}
