package eu.pb4.polymer.virtualentity.api.elements;

import eu.pb4.polymer.virtualentity.api.tracker.InteractionTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class InteractionElement extends GenericEntityElement {
    private InteractionHandler handler = InteractionHandler.EMPTY;

    public InteractionElement() {
    }

    public InteractionElement(InteractionHandler handler) {
        this.setHandler(handler);
    }

    public static InteractionElement redirect(Entity redirectedEntity) {
        return new InteractionElement(InteractionHandler.redirect(redirectedEntity));
    }

    public void setHandler(InteractionHandler handler) {
        this.handler = handler;
    }

    @Override
    public InteractionHandler getInteractionHandler(ServerPlayerEntity player) {
        return this.handler;
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
        setWidth(dimensions.width);
        setHeight(dimensions.height);
    }

    public boolean shouldRespond() {
        return this.dataTracker.get(InteractionTrackedData.RESPONSE);
    }
}
