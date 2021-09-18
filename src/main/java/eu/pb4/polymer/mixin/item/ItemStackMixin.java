package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.item.ItemHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyArg(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;<init>(Ljava/lang/String;)V", ordinal = 3))
    private String polymer_changeId(String id) {
        ItemStack stack = (ItemStack) (Object) this;
        return stack.hasNbt() && stack.getNbt().contains(ItemHelper.VIRTUAL_ITEM_ID) ? stack.getNbt().getString(ItemHelper.VIRTUAL_ITEM_ID) : id;
    }
}
