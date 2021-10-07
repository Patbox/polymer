package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.BlockHelper;
import eu.pb4.polymer.block.VirtualBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerActionResponseS2CPacket.class)
public class PlayerActionResponseS2CPacketMixin {
    @Shadow @Final private BlockState state;

    @Unique BlockState cachedBlockState = null;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtualBlockState(BlockState state) {
        if (state.getBlock() instanceof VirtualBlock virtualBlock) {
            if (this.cachedBlockState == null) {
                this.cachedBlockState = BlockHelper.getBlockStateSafely(virtualBlock, state);
            }
            return this.cachedBlockState;
        }
        return state;
    }


    @Environment(EnvType.CLIENT)
    @Inject(method = "pos", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        if (this.state.getBlock() instanceof VirtualBlock virtualBlock) {
            if (this.cachedBlockState == null) {
                this.cachedBlockState = BlockHelper.getBlockStateSafely(virtualBlock, state);
            }
            cir.setReturnValue(this.cachedBlockState);
        }
    }
}
