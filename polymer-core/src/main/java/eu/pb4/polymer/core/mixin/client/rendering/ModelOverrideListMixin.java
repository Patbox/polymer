package eu.pb4.polymer.core.mixin.client.rendering;

import eu.pb4.polymer.core.impl.client.CoreClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin {
    @ModifyVariable(method = "apply", at = @At("HEAD"), require = 0)
    private ItemStack polymer$replaceItemStack(ItemStack stack) {
        return CoreClientUtils.getRenderingStack(stack);
    }
}
