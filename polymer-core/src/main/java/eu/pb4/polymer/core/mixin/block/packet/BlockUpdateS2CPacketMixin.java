package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockUpdateS2CPacket.class, priority = 500)
public class BlockUpdateS2CPacketMixin {
    @Shadow private BlockState state;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V"))
    private Object polymer_replaceWithVirtualBlockState(Object state) {
        return PolymerBlockUtils.getPolymerBlockState((BlockState) state, PolymerUtils.getPlayerContext());
    }


    @Environment(EnvType.CLIENT)
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(PolymerBlockUtils.getPolymerBlockState(this.state, PolymerUtils.getPlayerContext()));
    }
}
