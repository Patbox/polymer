package eu.pb4.polymer.other;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helpers {
    private static Map<EntityType<?>, Entity> EXAMPLE_ENTITIES = new HashMap<>();

    public static List<DataTracker.Entry<?>> getExampleTrackedDataOfEntityType(EntityType<?> type) {
        Entity entity = EXAMPLE_ENTITIES.get(type);

        if (entity == null) {
            entity = type.create(FakeWorld.INSTANCE);
            EXAMPLE_ENTITIES.put(type, entity);
        }

        return entity.getDataTracker().getAllEntries();
    }
}
