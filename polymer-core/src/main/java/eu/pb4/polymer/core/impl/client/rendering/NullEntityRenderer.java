package eu.pb4.polymer.core.impl.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class NullEntityRenderer extends NoopRenderer<Entity> {


    public NullEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void submit(EntityRenderState renderState, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        super.submit(renderState, matrices, queue, cameraRenderState);
        var text = "NO RENDERER: " + BuiltInRegistries.ENTITY_TYPE.getKey(renderState.entityType);

        matrices.pushPose();
        matrices.translate(0, renderState.boundingBoxHeight / 2, 0);
        matrices.mulPose(cameraRenderState.orientation);
        matrices.scale(0.025F, -0.025F, 0.025F);
        Font textRenderer = this.getFont();
        float f = (float)(-textRenderer.width(text)) / 2.0F;
        int j = (int)(Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        queue.submitText(matrices, f, 1, Component.literal(text).getVisualOrderText(), true, Font.DisplayMode.NORMAL, renderState.lightCoords, 0xbb3333, j, 0);
        matrices.popPose();

    }
}
