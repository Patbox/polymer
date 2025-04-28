package eu.pb4.polymer.core.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.impl.client.rendering.NullEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.EntityRenderers;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(EntityRenderers.class)
public class EntityRenderersMixin {
    @ModifyReturnValue(method = "reloadEntityRenderers", at = @At("TAIL"))
    private static Map<EntityType<?>, EntityRenderer<?, ?>> polymer$replaceEntityRenderer(Map<EntityType<?>, EntityRenderer<?, ?>> original,
                                                                                          @Local(argsOnly = true) EntityRendererFactory.Context ctx) {
        var entityMap = new IdentityHashMap<EntityType<?>, EntityRenderer<?, ?>>();

        for (var ent : Registries.ENTITY_TYPE) {
            if (PolymerEntityUtils.isPolymerEntityType(ent) && !original.containsKey(ent)) {
                if (entityMap.isEmpty()) {
                    entityMap.putAll(original);
                }
                entityMap.put(ent, new NullEntityRenderer(ctx));
            }
        }
        return entityMap.isEmpty() ? original : entityMap;
    }
}
