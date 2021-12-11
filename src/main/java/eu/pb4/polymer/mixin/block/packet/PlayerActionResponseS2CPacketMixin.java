package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.client.PolymerClientDecoded;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.PlayerActionResponseS2CPacket;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerActionResponseS2CPacket.class)
public abstract class PlayerActionResponseS2CPacketMixin {
    @Shadow @Final private BlockState state;

    @Shadow public abstract BlockState state();

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtualBlockState(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state(), PolymerUtils.getPlayer());
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;get(I)Ljava/lang/Object;"))
    private static Object polymer_replaceState(IdList instance, int index) {
        if (index >= PolymerBlockUtils.BLOCK_STATE_OFFSET) {
            return InternalClientRegistry.getRealBlockState(index - PolymerBlockUtils.BLOCK_STATE_OFFSET + 1);
        }

        return instance.get(index);
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "state", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        if (this.state.getBlock() instanceof PolymerBlock virtualBlock && !PolymerClientDecoded.checkDecode(virtualBlock)) {
            cir.setReturnValue(PolymerBlockUtils.getBlockStateSafely(virtualBlock, state, PolymerUtils.getPlayer()));
        }
    }
}
