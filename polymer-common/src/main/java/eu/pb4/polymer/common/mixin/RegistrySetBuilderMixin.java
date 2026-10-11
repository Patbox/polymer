package eu.pb4.polymer.common.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import eu.pb4.polymer.common.impl.CommonImpl;
import net.minecraft.core.RegistrySetBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(RegistrySetBuilder.class)
public class RegistrySetBuilderMixin {
    @WrapWithCondition(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistrySetBuilder$BootstrappedRegistryState;errorOnMissingHolders(Lnet/minecraft/core/RegistrySetBuilder$BuildState;)V"))
    private boolean skipFreezeSometimes2(@Coerce Object instance, @Coerce Object state) {
        return !CommonImpl.SKIP_REGISTRY_VALIDATION.isBound();
    }

    @WrapWithCondition(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistrySetBuilder$BuildState;throwOnError()V"))
    private boolean skipFreezeSometimes3(@Coerce Object instance) {
        return !CommonImpl.SKIP_REGISTRY_VALIDATION.isBound();
    }
}
