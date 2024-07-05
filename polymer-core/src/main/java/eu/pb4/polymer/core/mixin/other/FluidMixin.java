package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluid.class)
public class FluidMixin {
    @Shadow @Final public static IdList<FluidState> STATE_IDS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void polymer$enableMapping(CallbackInfo ci) {
        ((PolymerIdList<FluidState>) STATE_IDS).polymer$setChecker(
                x -> x.getFluid() instanceof PolymerObject,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) Registries.FLUID, x.getFluid()),
                x -> "(Fluid) " + Registries.FLUID.getId(x.getFluid()));
    }
}
