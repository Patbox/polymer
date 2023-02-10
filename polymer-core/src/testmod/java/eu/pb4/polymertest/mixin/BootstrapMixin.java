package eu.pb4.polymertest.mixin;

import eu.pb4.polymertest.TestMod;
import net.minecraft.Bootstrap;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Registries.class)
public class BootstrapMixin {
    @Inject(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registries;init()V", shift = At.Shift.AFTER))
    private static void test$run(CallbackInfo ci) {
        //TestMod.onInitialize();
    }
}
