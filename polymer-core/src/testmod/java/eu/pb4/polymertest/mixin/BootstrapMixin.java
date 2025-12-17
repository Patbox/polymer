package eu.pb4.polymertest.mixin;

import eu.pb4.polymertest.TestMod;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BootstrapMixin {
    @Inject(method = "bootStrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;createContents()V", shift = At.Shift.AFTER))
    private static void test$run(CallbackInfo ci) {
        //TestMod.onInitialize();
    }
}
