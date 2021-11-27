package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$18")
public class TrackedDataHandlerRegistryBlockStateMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithPolymer(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state);
    }
}
