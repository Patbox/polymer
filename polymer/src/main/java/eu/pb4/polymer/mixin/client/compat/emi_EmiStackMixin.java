package eu.pb4.polymer.mixin.client.compat;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import eu.pb4.polymer.api.item.PolymerItemUtils;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(EmiStack.class)
public abstract class emi_EmiStackMixin {
    @Shadow
    public abstract NbtCompound getNbt();

    @Shadow
    public abstract boolean hasNbt();

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;)Z", at = @At("TAIL"), cancellable = true, remap = false)
    private void polymer_areEqual(EmiStack stack, CallbackInfoReturnable<Boolean> cir) {
        var id1 = stack.hasNbt() ? stack.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : "";
        var id2 = this.hasNbt() ? this.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : "";
        if (!id1.equals(id2)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isEqual(Ldev/emi/emi/api/stack/EmiStack;Ldev/emi/emi/api/stack/Comparison;)Z", at = @At("TAIL"), cancellable = true, remap = false)
    private void polymer_areEqual2(EmiStack stack, Comparison comparison, CallbackInfoReturnable<Boolean> cir) {
        var id1 = stack.hasNbt() ? stack.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : "";
        var id2 = this.hasNbt() ? this.getNbt().getString(PolymerItemUtils.POLYMER_ITEM_ID) : "";
        if (!id1.equals(id2)) {
            cir.setReturnValue(false);
        }
    }
}
