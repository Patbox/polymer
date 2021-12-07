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
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockUpdateS2CPacket.class, priority = 500)
public class BlockUpdateS2CPacketMixin {
    @Shadow private BlockState state;

    @Environment(EnvType.CLIENT)
    @Unique private BlockState polymer_cachedBlockState = null;

    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithVirtualBlockState(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state, PolymerUtils.getPlayer());
    }


    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(IdList instance, int index) {
        if (instance == Block.STATE_IDS && index > PolymerBlockUtils.BLOCK_STATE_OFFSET) {
            var state = InternalClientRegistry.BLOCK_STATES.get(index - PolymerBlockUtils.BLOCK_STATE_OFFSET);

            if (state != null && state.realServerBlockState() != null && PolymerClientDecoded.checkDecode(state.realServerBlockState().getBlock())) {
                return state.realServerBlockState();
            }
        }

        return instance.get(index);
    }


    @Environment(EnvType.CLIENT)
    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    public void polymer_replaceWithVirtualState(CallbackInfoReturnable<BlockState> cir) {
        if (this.state.getBlock() instanceof PolymerBlock virtualBlock) {
            if (this.polymer_cachedBlockState == null) {
                this.polymer_cachedBlockState = PolymerBlockUtils.getBlockStateSafely(virtualBlock, state, PolymerUtils.getPlayer());
            }

            cir.setReturnValue(this.polymer_cachedBlockState);
        }
    }
}
