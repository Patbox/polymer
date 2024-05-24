package eu.pb4.polymer.rsm.mixin;

import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.minecraft.registry.Registries;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;startTimerHack()V"), method = "main")
    private static void afterModInit(CallbackInfo info) {
        for (var reg : Registries.REGISTRIES) {
            if (reg instanceof RegistrySyncExtension<?> ext) {
                ext.polymer_registry_sync$reorderEntries();
            }
        }
    }
}
