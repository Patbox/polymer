package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import eu.pb4.polymer.core.mixin.block.ClientboundBlockEntityDataPacketAccessor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

@Mixin(ClientboundBlockEntityDataPacket.class)
public class ClientboundBlockEntityDataPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/StreamCodec;composite(Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/StreamCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEntityDataPacket> changeNbt(StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEntityDataPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            var context = PacketContext.get();
            if (packet.getTag() == null || packet.getTag().isEmpty() || context == null) {
                return packet;
            }
            var nbt = PolymerBlockUtils.transformBlockEntityNbt(context, packet.getType(), packet.getTag(), buf.registryAccess());
            if (packet.getTag() == nbt) {
                return packet;
            }
            return ClientboundBlockEntityDataPacketAccessor.createBlockEntityUpdateS2CPacket(packet.getPos(), packet.getType(), nbt);
        });
    }
}
