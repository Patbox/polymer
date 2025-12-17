package eu.pb4.polymer.core.mixin.client.compat.fabric;

import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FabricItem.class)
public interface fabricItem_FabricItemMixin {
    @Inject(method = "getCreatorNamespace", at = @At("HEAD"), cancellable = true, require = 0)
    private void injectPolymerCreatorName(ItemStack stack, CallbackInfoReturnable<String> cir) {
        var modId = PolymerImplUtils.getModName(stack);
        if (modId != null) {
            cir.setReturnValue(modId);
        }
    }
}
