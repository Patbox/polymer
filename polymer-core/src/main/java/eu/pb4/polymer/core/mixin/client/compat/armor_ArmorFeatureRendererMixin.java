package eu.pb4.polymer.core.mixin.client.compat;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
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
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public class armor_ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {
    @Inject(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/DyeableArmorItem;getColor(Lnet/minecraft/item/ItemStack;)I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT, require = 0)
    private void polymer_changeArmorTexture(MatrixStack matrices, VertexConsumerProvider vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci, ItemStack stack) {
        if (InternalClientRegistry.hasArmorTextures) {
            int color = ((DyeableArmorItem) stack.getItem()).getColor(stack);

            if (InternalClientRegistry.ARMOR_TEXTURES_1.containsKey(color)) {
                boolean usesSecondLayer = armorSlot == EquipmentSlot.LEGS;
                VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getArmorCutoutNoCull((usesSecondLayer ? InternalClientRegistry.ARMOR_TEXTURES_2 : InternalClientRegistry.ARMOR_TEXTURES_1).get(color)), false, stack.hasGlint());
                model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1f, 1f, 1f, 1.0F);
                ci.cancel();
            }
        }
    }
}
