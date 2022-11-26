package eu.pb4.polymer.resourcepack.mixin.compat.polymc;

import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.poly.item.FancyPantsItemPoly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(FancyPantsItemPoly.class)
public class polymc_FancyPantsItemPolyMixin {
    @Inject(method = "onFirstRegister", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polymer_cancelReplacement(PolyRegistry leatherItem, CallbackInfo ci) {
        ci.cancel();
    }
}
