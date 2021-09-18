package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public class ItemGroupMixin {
    @Shadow private ItemStack icon;

    @Inject(method = "getIcon", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemGroup;createIcon()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER))
    private void polymer_wrapIcon(CallbackInfoReturnable<ItemStack> cir) {
        if (this.icon.getItem() instanceof VirtualItem virtualItem) {
            this.icon = virtualItem.getVirtualItemStack(this.icon, null);
        }
    }
}
