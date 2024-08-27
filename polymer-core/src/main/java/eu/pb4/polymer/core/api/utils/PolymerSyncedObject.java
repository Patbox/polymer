package eu.pb4.polymer.core.api.utils;

import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Used to mark client-synchronized polymer objects like BlockEntities, Enchantments, Recipes, etc
 */

public interface PolymerSyncedObject<T> extends PolymerObject {

    /**
     * Generic method to get polymer replacement sent to player
     *
     * @param player target player
     * @return a replacement. It shouldn't be a null unless specified otherwise
     */
    T getPolymerReplacement(ServerPlayerEntity player);

    /**
     * Allows to gate syncing of this object with clients running polymer
     */
    default boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return true;
    }

    /**
     * Allows to mark it to still send it to supported clients (for client optional setups)
     * Currently used for tags
     */
    default boolean canSyncRawToClient(@Nullable ServerPlayerEntity player) {
        return false;
    }

    @Deprecated
    static boolean canSyncRawToClient(Object obj, ServerPlayerEntity player) {
        return obj instanceof PolymerSyncedObject<?> pol ? pol.canSyncRawToClient(player) : !PolymerUtils.isServerOnly(obj);
    }

    static <T> boolean canSyncRawToClient(Registry<T> registry, T obj, ServerPlayerEntity player) {
        return obj instanceof PolymerSyncedObject<?> pol ? pol.canSyncRawToClient(player) : !PolymerUtils.isServerOnly(registry, obj);
    }
}
