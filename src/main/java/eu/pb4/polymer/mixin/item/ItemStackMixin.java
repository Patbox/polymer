package eu.pb4.polymer.mixin.item;

import eu.pb4.polymer.api.item.PolymerItemUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.other.PolymerTooltipContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer_dontMultiplyIds(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if (context instanceof PolymerTooltipContext) {
            cir.setReturnValue(list);
        }
    }

    @ModifyArg(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;<init>(Ljava/lang/String;)V", ordinal = 3))
    private String polymer_changeId(String id) {
        ItemStack stack = (ItemStack) (Object) this;
        return stack.hasNbt() && stack.getNbt().contains(PolymerItemUtils.POLYMER_ITEM_ID) ? stack.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : id;
    }
}
