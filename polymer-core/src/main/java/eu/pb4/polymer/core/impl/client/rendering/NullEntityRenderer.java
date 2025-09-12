package eu.pb4.polymer.core.impl.client.rendering;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EmptyEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class NullEntityRenderer extends EmptyEntityRenderer<Entity> {


    public NullEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(EntityRenderState renderState, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        var text = "NO RENDERER: " + Registries.ENTITY_TYPE.getId(renderState.entityType);

        matrices.push();
        matrices.translate(0, renderState.height / 2, 0);
        matrices.multiply(this.dispatcher.getRotation());
        matrices.scale(0.025F, -0.025F, 0.025F);
        TextRenderer textRenderer = this.getTextRenderer();
        float f = (float)(-textRenderer.getWidth(text)) / 2.0F;
        int j = (int)(MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F) * 255.0F) << 24;
        queue.submitText(matrices, f, 1, Text.literal(text).asOrderedText(), true, TextRenderer.TextLayerType.NORMAL, renderState.light, 0xbb3333, j, 0);
        matrices.pop();

    }
}
