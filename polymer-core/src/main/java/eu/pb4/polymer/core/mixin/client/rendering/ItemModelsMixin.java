package eu.pb4.polymer.core.mixin.client.rendering;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.impl.client.CoreClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @ModifyVariable(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"), require = 0)
    private ItemStack polymer$replaceItemStack(ItemStack stack) {
        return CoreClientUtils.getRenderingStack(stack, ClientUtils.getLookup());
    }
}
