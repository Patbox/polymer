package eu.pb4.polymer.core.mixin.client.compat;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.client.compat.CompatUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Environment(EnvType.CLIENT)
@Mixin(EmiStack.class)
public abstract class emi_EmiStackMixin {
    @Shadow public abstract ItemStack getItemStack();

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$areEqual(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!CompatUtils.areSamePolymerType(stack.getItemStack(), this.getItemStack())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;Ldev/emi/emi/api/stack/Comparison;)Z", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void polymer$areEqual2(EmiStack stack, Comparison comparison, CallbackInfoReturnable<Boolean> cir) {
        if (!CompatUtils.areSamePolymerType(stack.getItemStack(), this.getItemStack())) {
            cir.setReturnValue(false);
        }
    }
}
