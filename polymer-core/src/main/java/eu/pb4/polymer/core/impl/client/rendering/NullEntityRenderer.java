package eu.pb4.polymer.core.impl.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class NullEntityRenderer extends EmptyEntityRenderer<Entity> {
    public static final EntityRenderer<Entity> INSTANCE = new NullEntityRenderer();

    private NullEntityRenderer() {
        super(new EntityRendererFactory.Context(null, null, null, null, null, null, null));
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

    }

    @Override
    protected void renderLabelIfPresent(Entity entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

    }
}
