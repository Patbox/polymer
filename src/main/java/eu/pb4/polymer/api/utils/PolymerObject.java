package eu.pb4.polymer.api.utils;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Used to mark general polymer objects like BlockEntities, Enchantments, Recipe Serializers, etc
 */
public interface PolymerObject {
    default boolean syncWithPolymerClients(ServerPlayerEntity player) {
        return true;
    }
}
