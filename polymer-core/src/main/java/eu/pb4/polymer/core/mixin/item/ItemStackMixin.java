package eu.pb4.polymer.core.mixin.item;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.ItemStackAwareNbtCompound;
import eu.pb4.polymer.core.impl.other.PolymerTooltipContext;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
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
        if (nbt.contains(ItemStackAwareNbtCompound.MARKER_KEY) && PolymerCommonUtils.isServerBound()) {
            return PolymerItemUtils.getRealItemStack(stack);
        }

        return stack;
    }*/

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void polymerCore$magicPerPlayerNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (PolymerImpl.ITEMSTACK_NBT_HACK) {
            var self = (ItemStack) (Object) this;
            var player = PolymerUtils.getPlayerContext();
            if (PolymerItemUtils.isPolymerServerItem(self, player)) {
                ((ItemStackAwareNbtCompound) nbt).polymerCore$setItemStack(true);
            }
        }
    }


    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/item/TooltipContext;isAdvanced()Z", ordinal = 2), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void polymer$quitEarly(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List<Text> list) {
        if (context instanceof PolymerTooltipContext) {
            cir.setReturnValue(list);
        }
    }
}
