package eu.pb4.polymer.impl.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class NullEntityRenderer extends EntityRenderer<Entity> {
    public static final EntityRenderer<Entity> INSTANCE = new NullEntityRenderer();

    private NullEntityRenderer() {
        super(new EntityRendererFactory.Context(null, null, null, null, null, null, null));
    }


    @Override
    protected int getBlockLight(Entity entity, BlockPos pos) {
        return 0;
    }

    @Override
    protected int getSkyLight(Entity entity, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    public void render(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

    }

    @Override
    public Identifier getTexture(Entity entity) {
        return new Identifier("minecraft", "pig");
    }
}
