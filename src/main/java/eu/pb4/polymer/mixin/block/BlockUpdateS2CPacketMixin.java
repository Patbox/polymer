package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockUpdateS2CPacket.class, priority = 500)
public class BlockUpdateS2CPacketMixin {
    @Shadow private BlockState state;

    @Environment(EnvType.CLIENT)
    @Unique private BlockState polymer_cachedBlockState = null;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtualBlockState(BlockState state) {
        if (state.getBlock() instanceof VirtualBlock virtualBlock) {
            return BlockHelper.getBlockStateSafely(virtualBlock, state);
        }
        return state;
    }


    @Environment(EnvType.CLIENT)
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        if (this.state.getBlock() instanceof VirtualBlock virtualBlock) {
            if (this.polymer_cachedBlockState == null) {
                this.polymer_cachedBlockState = BlockHelper.getBlockStateSafely(virtualBlock, state);
            }

            cir.setReturnValue(this.polymer_cachedBlockState);
        }
    }
}
