package eu.pb4.polymer.ext.blocks.mixin.polymc;

import eu.pb4.polymer.ext.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.ext.blocks.impl.PolymerTextureBlockPoly;
import io.github.theepicblock.polymc.api.PolyRegistry;
import io.github.theepicblock.polymc.impl.generator.BlockPolyGenerator;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BlockPolyGenerator.class, priority = 1100)
public class BlockPolyGeneratorMixin {

    @Inject(method = "generatePoly", at = @At("HEAD"), cancellable = true, remap = false)
    private static void polymer_addVirtualBlockPoly(Block block, PolyRegistry builder, CallbackInfoReturnable cir) {
        if (block instanceof PolymerTexturedBlock) {
            cir.setReturnValue(new PolymerTextureBlockPoly());
        }
    }

}
