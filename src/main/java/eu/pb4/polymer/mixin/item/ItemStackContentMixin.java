package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HoverEvent.ItemStackContent.class)
public class ItemStackContentMixin {
    @ModifyVariable(method = "<init>(Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), ordinal = 0)
    private static ItemStack replaceWithVirtual(ItemStack stack) {
        return ItemHelper.getVirtualItemStack(stack, null);
    }
}
