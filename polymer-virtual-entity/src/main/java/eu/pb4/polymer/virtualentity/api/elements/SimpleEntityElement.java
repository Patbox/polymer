package eu.pb4.polymer.virtualentity.api.elements;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;

public class SimpleEntityElement extends GenericEntityElement{
    private final EntityType<?> type;

    public SimpleEntityElement(EntityType<?> type) {
        this.type = type;
    }

    @Override
    protected EntityType<? extends Entity> getEntityType() {
        return this.type;
    }
}
