package eu.pb4.polymer.api.utils;

/**
 * Used to mark general polymer objects like BlockEntities, Enchantments, Recipe Serializers, etc
 */
public interface PolymerObject {
    default boolean syncWithPolymerClients() {
        return true;
    }
}
