package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.common.impl.client.ClientUtils;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
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
public abstract class ChunkDeltaUpdateS2CPacketMixin {
    @ModifyArg(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState polymer$replaceWithPolymerBlockState(BlockState state) {
        return PolymerBlockUtils.getPolymerBlockState(state, PolymerUtils.getPlayerContext());
    }

    @Environment(EnvType.CLIENT)
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/IdList;get(I)Ljava/lang/Object;"), require = 0)
    private Object polymer$decodeState(IdList instance, int index) {
        return InternalClientRegistry.decodeState(index);
    }

    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "visitUpdates", at = @At(value = "INVOKE", target = "Ljava/util/function/BiConsumer;accept(Ljava/lang/Object;Ljava/lang/Object;)V"), index = 1)
    private Object polymer$replaceState(Object obj) {
        return obj != null ? PolymerBlockUtils.getPolymerBlockState((BlockState) obj, ClientUtils.getPlayer()) : null;
    }
}
