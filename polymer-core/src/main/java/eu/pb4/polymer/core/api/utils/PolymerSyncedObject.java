package eu.pb4.polymer.core.api.utils;

import net.minecraft.server.network.ServerPlayerEntity;

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
     */
    default boolean canSyncRawToClient(ServerPlayerEntity player) {
        return false;
    }

    static boolean canSyncRawToClient(Object obj, ServerPlayerEntity player) {
        return obj instanceof PolymerSyncedObject pol ? pol.canSyncRawToClient(player) : !(obj instanceof PolymerObject);
    }
}
