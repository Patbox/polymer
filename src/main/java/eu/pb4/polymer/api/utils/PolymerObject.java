package eu.pb4.polymer.api.utils;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

/**
 * Used to mark general polymer objects like BlockEntities, Enchantments, Recipe Serializers, etc
 */
public interface PolymerObject {
    Predicate<Object> PREDICATE = (obj) -> obj instanceof PolymerObject;

    /**
     * Allows to gate syncing of this object with clients running polymer
     */
    default boolean shouldSyncWithPolymerClient(ServerPlayerEntity player) {
        return true;
    }

    static boolean is(Object obj) {
        return obj instanceof PolymerObject;
    }
}
