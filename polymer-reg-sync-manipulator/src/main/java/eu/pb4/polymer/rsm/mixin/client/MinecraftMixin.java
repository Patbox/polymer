package eu.pb4.polymer.rsm.mixin.client;

import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
    private void afterModInit(CallbackInfo ci) {
        for (var reg : BuiltInRegistries.REGISTRY) {
            if (reg instanceof RegistrySyncExtension<?> ext) {
                ext.polymer_registry_sync$reorderEntries();
            }
        }
    }
}
