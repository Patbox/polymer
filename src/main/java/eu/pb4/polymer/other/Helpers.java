package eu.pb4.polymer.other;

import eu.pb4.polymer.PolymerMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public class Helpers {
    private static final Map<EntityType<?>, @Nullable Entity> EXAMPLE_ENTITIES = new HashMap<>();
    private static final PigEntity PIG = new PigEntity(EntityType.PIG, FakeWorld.INSTANCE);

    public static List<DataTracker.Entry<?>> getExampleTrackedDataOfEntityType(EntityType<?> type) {
        return getEntity(type).getDataTracker().getAllEntries();
    }

    public static boolean isLivingEntity(EntityType<?> type) {
        return getEntity(type) instanceof LivingEntity;
    }

    public static boolean isMobEntity(EntityType<?> type) {
        return getEntity(type) instanceof MobEntity;
    }

    private static Entity getEntity(EntityType<?> type) {
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

        return entity;
    }

    public static Entity getFakeEntity() {
        return FakeEntity.INSTANCE;
    }

    public static PigEntity getTacticalPig() {
        return PIG;
    }
}
