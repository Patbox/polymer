package eu.pb4.polymer.resourcepack.mixin.client.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public class armor_ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {
    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmorParts(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/model/BipedEntityModel;FFFLnet/minecraft/util/Identifier;)V"), require = 0)
    private void polymer$changeArmorTexture(ArmorFeatureRenderer instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, float red, float green, float blue, Identifier texture, Operation<Void> original, @Local ItemStack stack) {
        if (PolymerResourcePackMod.hasArmorTextures) {
            var color = stack.get(DataComponentTypes.DYED_COLOR);

            if (color != null && PolymerResourcePackMod.ARMOR_TEXTURES_1.containsKey(color.rgb())) {
                boolean usesSecondLayer = texture.getPath().endsWith("_2");
                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull((usesSecondLayer ? PolymerResourcePackMod.ARMOR_TEXTURES_2 : PolymerResourcePackMod.ARMOR_TEXTURES_1).get(color.rgb())), false, stack.hasGlint());
                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1.0F);
                return;
            }
        }

        original.call(instance, matrices, vertexConsumers, light, model, red, green, blue, texture);
    }
}
