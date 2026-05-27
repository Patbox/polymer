package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.data.InteractionEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

public class InteractionElement extends GenericEntityElement {

    public InteractionElement() {
    }

    public InteractionElement(InteractionHandler handler) {
        this.setInteractionHandler(handler);
    }

    public static InteractionElement redirect(Entity redirectedEntity) {
        return new InteractionElement(InteractionHandler.redirect(redirectedEntity));
    }

    @Override
    protected final EntityType<? extends Entity> getEntityType() {
        return EntityTypes.INTERACTION;
    }

    public float getWidth() {
        return this.syncedData.get(InteractionEntityData.WIDTH);
    }

    public void setWidth(float width) {
        this.syncedData.set(InteractionEntityData.WIDTH, width);
    }

    public float getHeight() {
        return this.syncedData.get(InteractionEntityData.HEIGHT);
    }

    public void setHeight(float height) {
        this.syncedData.set(InteractionEntityData.HEIGHT, height);
    }

    public void setResponse(boolean response) {
        this.syncedData.set(InteractionEntityData.RESPONSE, response);
    }

    public void setSize(float width, float height) {
        setWidth(width);
        setHeight(height);
    }

    public void setSize(EntityDimensions dimensions) {
        setWidth(dimensions.width());
        setHeight(dimensions.height());
    }

    public boolean shouldRespond() {
        return this.syncedData.get(InteractionEntityData.RESPONSE);
    }
}
