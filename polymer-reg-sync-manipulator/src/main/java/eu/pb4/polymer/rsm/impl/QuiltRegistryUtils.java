package eu.pb4.polymer.rsm.impl;

import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

@ApiStatus.Internal
public final class QuiltRegistryUtils {
    private static Logger LOGGER = LoggerFactory.getLogger("Polymer Registry Sync Manipulator");
    private static Method markAsOptional = null;
    private static Method isOptional = null;
    private static boolean triedOnce1 = false;
    private static boolean triedOnce2 = false;

    public static void markAsOptional(Registry<?> registry, Object entry) {
        if (CompatStatus.QUILT_REGISTRY) {
            try {
                if (markAsOptional == null && !triedOnce1) {
                    triedOnce1 = true;
                    markAsOptional = Class.forName("org.quiltmc.qsl.registry.api.sync.RegistrySynchronization").getMethod("setEntryOptional", SimpleRegistry.class, Object.class);
                }

                if (markAsOptional != null) {
                    markAsOptional.invoke(null, registry, entry);
                }
            } catch (Throwable t) {
                LOGGER.warn("Quilt Registry module is present, but there is no RegistrySynchronization#setEntryOptional(SimpleRegistry<T>, T). Error: {}", t.getMessage());
                markAsOptional = null;
            }
        }
    }

    public static boolean isOptional(Registry<?> registry, Object entry) {
        if (CompatStatus.QUILT_REGISTRY) {
            try {
                if (isOptional == null && !triedOnce2) {
                    triedOnce2 = true;
                    isOptional = Class.forName("org.quiltmc.qsl.registry.api.sync.RegistrySynchronization").getMethod("isEntryOptional", SimpleRegistry.class, Object.class);
                }

                if (isOptional != null) {
                    return isOptional.invoke(null, registry, entry) == Boolean.TRUE;
                }
            } catch (Throwable t) {
                LOGGER.warn("Quilt Registry module is present, but there is no RegistrySynchronization#isEntryOptional(SimpleRegistry<T>, T). Error: {}", t.getMessage());
                isOptional = null;
            }
        }

        return false;
    }
}
