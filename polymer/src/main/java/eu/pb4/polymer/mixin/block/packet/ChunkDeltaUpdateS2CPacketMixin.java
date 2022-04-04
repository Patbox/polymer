package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.api.block.PolymerBlockUtils;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.client.InternalClientRegistry;
import eu.pb4.polymer.impl.interfaces.PlayerAwarePacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ChunkDeltaUpdateS2CPacket.class, priority = 500)
public class ChunkDeltaUpdateS2CPacketMixin implements PlayerAwarePacket {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer_replaceWithPolymerBlockState(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state, PolymerUtils.getPlayer());
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;get(I)Ljava/lang/Object;"))
    private Object polymer_replaceState(IdList instance, int index) {
        return InternalClientRegistry.decodeState(index);
    }
}
