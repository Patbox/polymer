package eu.pb4.polymer.core.mixin.entity;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net/minecraft/entity/data/TrackedDataHandlerRegistry$2")
public class TrackedDataHandlerRegistryBlockStateMixin {
    @ModifyArg(method = "write(Lnet/minecraft/network/PacketByteBuf;Ljava/util/Optional;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithPolymer(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state, PolymerUtils.getPlayerContext());
    }
}
