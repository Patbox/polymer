package eu.pb4.polymer.mixin.block;

import eu.pb4.polymer.block.VirtualBlock;
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
}
