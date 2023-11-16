package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.DataTrackerLike;
import eu.pb4.polymer.virtualentity.api.tracker.SimpleDataTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class SimpleEntityElement extends GenericEntityElement {
    private static final ThreadLocal<EntityType<?>> LOCAL_TYPE = new ThreadLocal<>();
    private final EntityType<?> type;

    public SimpleEntityElement(EntityType<?> type) {
        this(type, hackyHack(type));
        LOCAL_TYPE.remove();
    }

    private static Object hackyHack(EntityType<?> type) {
        LOCAL_TYPE.set(type);
        return type;
    }

    private SimpleEntityElement(EntityType<?> type, Object hack) {
        super();
        this.type = type;
    }

    @Override
    protected DataTrackerLike createDataTracker() {
        if (this.type != null) {
            return super.createDataTracker();
        } else {
            return new SimpleDataTracker(LOCAL_TYPE.get());
        }
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return this.type;
    }
}
