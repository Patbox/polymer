package eu.pb4.polymer.mixin.client.compat;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Pseudo
@Mixin(EmiStack.class)
public abstract class emi_EmiStackMixin {
    @Shadow public abstract ItemStack getItemStack();

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;)Z", at = @At("TAIL"), cancellable = true, remap = false)
    private void polymer_areEqual(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {
        var id1 = PolymerItemUtils.getServerIdentifier(stack.getItemStack());
        var id2 = PolymerItemUtils.getServerIdentifier(this.getItemStack());
        if (!Objects.equals(id1, id2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;Ldev/emi/emi/api/stack/Comparison;)Z", at = @At("TAIL"), cancellable = true, remap = false)
    private void polymer_areEqual2(EmiStack stack, Comparison comparison, CallbackInfoReturnable<Boolean> cir) {
        var id1 = PolymerItemUtils.getServerIdentifier(stack.getItemStack());
        var id2 = PolymerItemUtils.getServerIdentifier(this.getItemStack());
        if (!Objects.equals(id1, id2)) {
            cir.setReturnValue(false);
        }
    }
}
