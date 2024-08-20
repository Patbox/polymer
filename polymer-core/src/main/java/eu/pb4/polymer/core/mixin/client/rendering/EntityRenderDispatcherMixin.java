package eu.pb4.polymer.core.mixin.client.rendering;

import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.core.impl.client.rendering.NullEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private void polymer$replaceEntityRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<?, ?>> cir) {
        if (PolymerKeepModel.useServerModel(entity)) {
            cir.setReturnValue(NullEntityRenderer.INSTANCE);
        }
    }
}
