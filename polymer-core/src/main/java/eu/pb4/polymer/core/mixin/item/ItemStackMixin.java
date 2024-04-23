package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import net.minecraft.client.item.TooltipType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isDamaged()Z"), cancellable = true)
    private void stopEarly(Item.TooltipContext context, @Nullable PlayerEntity player, TooltipType type, CallbackInfoReturnable<List<Text>> cir, @Local(ordinal = 0) List<Text> tooltip) {
        if (type instanceof PolymerTooltipType) {
            cir.setReturnValue(tooltip);
        }
    }
}
