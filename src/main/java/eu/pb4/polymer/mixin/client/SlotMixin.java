package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.PolymerUtils;
import eu.pb4.polymer.item.VirtualItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(method = "getStack", at = @At("TAIL"), cancellable = true)
    private void polymer_replaceStackOnClient(CallbackInfoReturnable<ItemStack> cir) {
        if (PolymerUtils.isOnClientSide()) {
            var og = cir.getReturnValue();
            if (og.getItem() instanceof VirtualItem virtualItem) {
                cir.setReturnValue(virtualItem.getVirtualItemStack(og, PolymerUtils.getPlayer()));
            }
        }
    }
}
