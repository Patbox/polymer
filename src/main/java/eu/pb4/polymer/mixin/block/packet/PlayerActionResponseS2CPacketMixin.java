package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
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

    @Unique BlockState polymer_cachedBlockState = null;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtualBlockState(BlockState state) {
        if (state.getBlock() instanceof PolymerBlock virtualBlock) {
            if (this.polymer_cachedBlockState == null) {
                this.polymer_cachedBlockState = PolymerBlockUtils.getBlockStateSafely(virtualBlock, state);
            }
            return this.polymer_cachedBlockState;
        }
        return state;
    }


    @Environment(EnvType.CLIENT)
    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        if (this.state.getBlock() instanceof PolymerBlock virtualBlock) {
            if (this.polymer_cachedBlockState == null) {
                this.polymer_cachedBlockState = PolymerBlockUtils.getBlockStateSafely(virtualBlock, state);
            }
            cir.setReturnValue(this.polymer_cachedBlockState);
        }
    }
}
