package eu.pb4.polymer.core.api.utils;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.Registry;
import org.jspecify.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

/**
 * Used to mark client-synchronized polymer objects like BlockEntities, Enchantments, Recipes, etc
 */

public interface PolymerSyncedObject<T> extends PolymerObject {
    /**
     * Generic method to get polymer replacement sent to player
     *
     * @param context target context
     * @return a replacement. It shouldn't be a null unless specified otherwise
     */
    T getPolymerReplacement(T object, PacketContext context);

    /**
     * Allows to gate syncing of this object with clients running polymer
     */
    default boolean canSynchronizeToPolymerClient(PacketContext context) {
        return true;
    }

    /**
     * Allows to mark it to still send it to supported clients (for client optional setups)
     * Currently used for tags
     */
    default boolean canSyncRawToClient(PacketContext context) {
        return false;
    }

    static <T> boolean canSyncRawToClient(Registry<T> registry, T obj, PacketContext context) {
        var pol = getSyncedObject(registry, obj);
        return pol != null ? pol.canSyncRawToClient(context) : !PolymerUtils.isServerOnly(registry, obj);
    }


    static <T> void setSyncedObject(Registry<T> registry, T obj, PolymerSyncedObject<T> object) {
        //noinspection unchecked
        setPlainSyncedObject(registry, obj, object);
        RegistrySyncUtils.setServerEntry(registry, obj);
    }

    static <T> void setPlainSyncedObject(Registry<T> registry, T obj, PolymerSyncedObject<T> object) {
        //noinspection unchecked
        ((RegistryExtension<T>) registry).polymer$setOverlay(obj, object);
    }

    @Nullable
    static <T> PolymerSyncedObject<T> getSyncedObject(Registry<T> registry, T obj) {
        if (obj instanceof PolymerSyncedObject<?> instance) {
            //noinspection unchecked
            return (PolymerSyncedObject<T>) instance;
        }

        return registry instanceof RegistryExtension<?> extension ? ((RegistryExtension<T>) extension).polymer$getOverlay(obj) : null;
    }

    static <T> boolean canSynchronizeToPolymerClient(Registry<T> registry, T entry, PacketContext ctx) {
        var obj = getSyncedObject(registry, entry);
        return obj == null || obj.canSynchronizeToPolymerClient(ctx);
    }
}
