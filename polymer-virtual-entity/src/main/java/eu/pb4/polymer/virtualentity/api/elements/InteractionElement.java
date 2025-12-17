package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;

public class InteractionElement extends GenericEntityElement {

    public InteractionElement() {
    }

    public InteractionElement(InteractionHandler handler) {
        this.setHandler(handler);
    }

    public static InteractionElement redirect(Entity redirectedEntity) {
        return new InteractionElement(InteractionHandler.redirect(redirectedEntity));
    }

    @Deprecated
    public void setHandler(InteractionHandler handler) {
        this.setInteractionHandler(handler);
    }

    @Override
    protected final EntityType<? extends Entity> getEntityType() {
        return EntityType.INTERACTION;
    }

    public float getWidth() {
        return this.dataTracker.get(InteractionTrackedData.WIDTH);
    }

    public void setWidth(float width) {
        this.dataTracker.set(InteractionTrackedData.WIDTH, width);
    }

    public float getHeight() {
        return this.dataTracker.get(InteractionTrackedData.HEIGHT);
    }

    public void setHeight(float height) {
        this.dataTracker.set(InteractionTrackedData.HEIGHT, height);
    }

    public void setResponse(boolean response) {
        this.dataTracker.set(InteractionTrackedData.RESPONSE, response);
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
        return this.dataTracker.get(InteractionTrackedData.RESPONSE);
    }
}
