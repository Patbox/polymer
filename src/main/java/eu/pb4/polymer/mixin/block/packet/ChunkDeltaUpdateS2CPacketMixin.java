package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlock;
import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
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
    private BlockState polymer_replaceWithPolymerBlockState(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state, PolymerUtils.getPlayer());
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "visitUpdates", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer_replaceBlockStateOnClient(Object state) {
        return PolymerBlockUtils.getPolymerBlockState((BlockState) state, PolymerUtils.getPlayer());
    }
}
