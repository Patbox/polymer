package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.PolymerIdList;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "setOutputStreams", at = @At("HEAD"))
    private static void polymer$enableMapping(CallbackInfo ci) {
        ((PolymerIdList<BlockState>) Block.STATE_IDS).polymer$setChecker(
                x -> x.getBlock() instanceof PolymerObject,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) Registries.BLOCK, x.getBlock()),
                x -> "(Block) " + Registries.BLOCK.getId(x.getBlock())
        );
        ((PolymerIdList<FluidState>) Fluid.STATE_IDS).polymer$setChecker(
                x -> x.getFluid() instanceof PolymerObject,
                x -> PolymerImplUtils.isServerSideSyncableEntry((Registry<Object>) (Object) Registries.FLUID, x.getFluid()),
                x -> "(Fluid) " + Registries.FLUID.getId(x.getFluid()));
    }
}
