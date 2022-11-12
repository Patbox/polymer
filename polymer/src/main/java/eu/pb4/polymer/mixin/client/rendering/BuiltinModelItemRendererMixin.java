package eu.pb4.polymer.mixin.client.rendering;

import eu.pb4.polymer.api.item.PolymerItem;
import eu.pb4.polymer.api.utils.PolymerKeepModel;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.ClientUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Environment(EnvType.CLIENT)
@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), require = 0)
    private ItemStack polymer_replaceItem(ItemStack stack) {
        return stack.getItem() instanceof PolymerItem item && !PolymerKeepModel.is(item) ? item.getPolymerItemStack(stack, PolymerUtils.getTooltipContext(ClientUtils.getPlayer()), ClientUtils.getPlayer()) : stack;
    }
}
