package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Registries.class, priority = 1500)
public class RegistriesMixin {
    @Inject(method = "freezeRegistries", at = @At("TAIL"))
    private static void reorderEntries(CallbackInfo ci) {
        ((PolymerIdList<?>) Block.STATE_IDS).polymer$reorderEntries();
        ((PolymerIdList<?>) Fluid.STATE_IDS).polymer$reorderEntries();
    }
}
