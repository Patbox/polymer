package eu.pb4.polymer.mixin.polymc;

import eu.pb4.polymer.block.VirtualBlock;
import eu.pb4.polymer.other.polymc.VirtualPoly;
import io.github.theepicblock.polymc.api.PolyRegistry;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPolyGenerator.class)
public class BlockPolyGeneratorMixin {

    @Inject(method = "generatePoly", at = @At("HEAD"), cancellable = true, remap = false)
    private static void addVirtualBlockPole(Block block, PolyRegistry builder, CallbackInfoReturnable cir) {
        if (block instanceof VirtualBlock) {
            cir.setReturnValue(new VirtualPoly());
        }
    }

}
