package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BuiltInRegistries.class, priority = 1500)
public class BuiltInRegistriesMixin {
    @Inject(method = "freeze", at = @At("TAIL"))
    private static void reorderEntries(CallbackInfo ci) {
        ((PolymerIdMapper<?>) Block.BLOCK_STATE_REGISTRY).polymer$reorderEntries();
        ((PolymerIdMapper<?>) Fluid.FLUID_STATE_REGISTRY).polymer$reorderEntries();
    }
}
