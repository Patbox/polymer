package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.impl.other.PolymerTooltipContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    /*@ModifyReturnValue(method = "fromNbt(Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
    private static ItemStack polymerCore$swapToRealStack(ItemStack stack, @Local NbtCompound nbt) {
        if (nbt.contains(TypeAwareNbtCompound.MARKER_KEY) && PolymerCommonUtils.isServerBound()) {
            return PolymerItemUtils.getRealItemStack(stack);
        }

        return stack;
    }*/


    // todo
    /*@Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", ordinal = 1), cancellable = true)
    private void polymer$quitEarly(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, @Local List<Text> list) {
        if (context instanceof PolymerTooltipContext) {
            cir.setReturnValue(list);
        }
    }*/
}
