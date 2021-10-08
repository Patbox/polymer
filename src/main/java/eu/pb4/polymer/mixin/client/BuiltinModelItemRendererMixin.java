package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {
    @ModifyVariable(method = "render", at = @At("HEAD"), require = 0)
    private ItemStack polymer_replaceItem(ItemStack stack) {
        return stack.getItem() instanceof VirtualItem item ? item.getVirtualItemStack(stack, PolymerUtils.getPlayer()) : stack;
    }
}
