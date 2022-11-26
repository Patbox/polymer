package eu.pb4.polymer.core.mixin.compat.polymc;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.impl.compat.polymc.PolymerBlockPoly;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(BlockPolyGenerator.class)
public class polymc_BlockPolyGeneratorMixin {

    @Inject(method = "generatePoly", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polymer_addVirtualBlockPoly(Block block, PolyRegistry builder, CallbackInfoReturnable cir) {
        if (block instanceof PolymerBlock) {
            cir.setReturnValue(new PolymerBlockPoly());
        }
    }

}
