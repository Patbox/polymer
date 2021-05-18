package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ChunkDeltaUpdateS2CPacket.class, priority = 500)
public class ChunkDeltaUpdateS2CPacketMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState replaceWithVirtualBlockState(BlockState state) {
        if (state.getBlock() instanceof VirtualBlock) {
            return ((VirtualBlock) state.getBlock()).getVirtualBlockState(state);
        }
        return state;
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "visitUpdates", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object replaceBlockStateOnClient(Object state) {
        if (((BlockState) state).getBlock() instanceof VirtualBlock) {
            return ((VirtualBlock) ((BlockState) state).getBlock()).getVirtualBlockState(((BlockState) state));
        }
        return state;
    }
}
