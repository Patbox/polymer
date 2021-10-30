package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.api.item.PolymerItem;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), require = 0)
    private ItemStack polymer_replaceItem(ItemStack stack) {
        return stack.getItem() instanceof PolymerItem item ? item.getPolymerItemStack(stack, PolymerUtils.getPlayer()) : stack;
    }
}
