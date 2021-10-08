package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin {
    @ModifyVariable(method = "apply", at = @At("HEAD"), require = 0)
    private ItemStack polymer_replaceItemStack(ItemStack stack) {
        return stack.getItem() instanceof VirtualItem item ? item.getVirtualItemStack(stack, PolymerUtils.getPlayer()) : stack;
    }
}
