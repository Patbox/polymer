package eu.pb4.polymer.other;

import eu.pb4.polymer.PolymerMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class Helpers {
    private static Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();

    public static List<DataTracker.Entry<?>> getExampleTrackedDataOfEntityType(EntityType<?> type) {
        Entity entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            try {
                entity = type.create(FakeWorld.INSTANCE);
            } catch (Exception e) {
                PolymerMod.LOGGER.warn(String.format("Couldn't create template entity of %s (%s)... Defaulting to empty", Registry.ENTITY_TYPE.getId(type), type.getBaseClass().toString()));
                entity = FakeEntity.INSTANCE;
            }
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity.getDataTracker().getAllEntries();
    }
}
