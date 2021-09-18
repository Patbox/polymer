package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
    @ModifyVariable(method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;", at = @At("HEAD"))
    private ItemStack polymer_replaceItemStack(ItemStack stack) {
        return stack.getItem() instanceof VirtualItem item ? item.getVirtualItemStack(stack, PolymerUtils.getPlayer()) : stack;
    }
}
