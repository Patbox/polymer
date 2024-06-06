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
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(ArmorFeatureRenderer.class)
public class armor_ArmorFeatureRendererMixin<T extends LivingEntity, M extends BipedEntityModel<T>, A extends BipedEntityModel<T>> {
    @WrapOperation(method = "renderArmor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ArmorMaterial;layers()Ljava/util/List;"))
    private List<ArmorMaterial.Layer> polymer$changeArmorTexture(ArmorMaterial instance, Operation<List<ArmorMaterial.Layer>> original, @Local ItemStack stack) {
        if (PolymerResourcePackMod.hasArmorTextures && instance == ArmorMaterials.LEATHER.value()) {
            var color = stack.get(DataComponentTypes.DYED_COLOR);
            if (color != null) {
                return PolymerResourcePackMod.ARMOR_TEXTURES.getOrDefault(color.rgb(), PolymerResourcePackMod.LEATHER_OVERRIDE);
            }

            return PolymerResourcePackMod.LEATHER_OVERRIDE;
        }

        return original.call(instance);
    }
}
