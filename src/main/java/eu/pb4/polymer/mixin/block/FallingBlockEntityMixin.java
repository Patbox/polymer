package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

    @ModifyArg(method = "createSpawnPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtual(BlockState state) {
        if (state.getBlock() instanceof VirtualBlock virtualBlock) {
            return BlockHelper.getBlockStateSafely(virtualBlock, state);
        } else {
            return state;
        }
    }
}
