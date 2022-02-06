package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.impl.interfaces.PolymerIdList;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {
    @Inject(method = "setOutputStreams", at = @At("HEAD"))
    private static void polymer_enableMapping(CallbackInfo ci) {
        ((PolymerIdList) Block.STATE_IDS).polymer_enableLazyBlockStates();
    }
}
