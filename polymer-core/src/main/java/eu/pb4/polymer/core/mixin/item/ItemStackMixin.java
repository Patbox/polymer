package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.core.impl.other.PolymerTooltipType;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(ItemStack.class)
public class ItemStackMixin {
    @ModifyExpressionValue(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/tooltip/TooltipType;isAdvanced()Z"))
    private boolean removeAdvanced(boolean original, @Local(ordinal = 0) TooltipType type) {
        return original && !(type instanceof PolymerTooltipType);
    }
}
