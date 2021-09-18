package eu.pb4.polymer.entity;

import net.minecraft.entity.EntityType;

import java.util.Arrays;
import java.util.HashSet;

public class EntityHelper {
    private static final HashSet<EntityType<?>> ENTITY_IDENTIFIERS = new HashSet<>();

    /**
     * Marks EntityTypes as server-side only
     *
     * @param types Entity Types
     */
    public static void registerVirtualEntityType(EntityType<?>... types) {
        ENTITY_IDENTIFIERS.addAll(Arrays.asList(types));
    }

    /**
     * Checks if EntityType is server-side only
     *
     * @param type EntityType
     */
    public static boolean isVirtualEntityType(EntityType<?> type) {
        return ENTITY_IDENTIFIERS.contains(type);
    }
}
