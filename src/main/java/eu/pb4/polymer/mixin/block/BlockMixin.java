package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Block.class)
public class BlockMixin {
    @ModifyVariable(method = "getRawIdFromState(Lnet/minecraft/block/BlockState;)I", at = @At("HEAD"))
    private static BlockState rawBlockStateOverwrite(BlockState state) {
        if (state.getBlock() instanceof VirtualBlock) {
            return ((VirtualBlock) state.getBlock()).getVirtualBlockState(state);
        }
        return state;
    }
}
