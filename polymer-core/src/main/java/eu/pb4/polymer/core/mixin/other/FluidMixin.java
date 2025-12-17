package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdMapper;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluid.class)
public class FluidMixin {
    @Shadow @Final public static IdMapper<FluidState> FLUID_STATE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polymer$enableMapping(CallbackInfo ci) {
        ((PolymerIdMapper<FluidState>) FLUID_STATE_REGISTRY).polymer$setChecker(
                x -> PolymerSyncedObject.getSyncedObject(BuiltInRegistries.FLUID, x.getType()) != null,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) BuiltInRegistries.FLUID, x.getType()),
                x -> "(Fluid) " + BuiltInRegistries.FLUID.getKey(x.getType()));
    }
}
