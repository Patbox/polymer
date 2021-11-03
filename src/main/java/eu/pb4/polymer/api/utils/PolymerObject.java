package eu.pb4.polymer.api.utils;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Used to mark general polymer objects like BlockEntities, Enchantments, Recipe Serializers, etc
 */
public interface PolymerObject {
    /**
     * Allows to gate syncing of this object with clients running polymer
     */
    default boolean shouldSyncWithPolymerClient(ServerPlayerEntity player) {
        return true;
    }
}
