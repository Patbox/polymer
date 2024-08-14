package eu.pb4.polymer.core.mixin.block.packet;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.core.impl.networking.TransformingPacketCodec;
import eu.pb4.polymer.core.mixin.block.BlockEntityUpdateS2CPacketAccessor;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(BlockEntityUpdateS2CPacket.class)
public class BlockEntityUpdateS2CPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/PacketCodec;tuple(Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lnet/minecraft/network/codec/PacketCodec;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function3;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<RegistryByteBuf, BlockEntityUpdateS2CPacket> changeNbt(PacketCodec<RegistryByteBuf, BlockEntityUpdateS2CPacket> original) {
        return TransformingPacketCodec.encodeOnly(original, (buf, packet) -> {
            if (packet.getNbt() == null) {
                return packet;
            }
            var nbt = PolymerBlockUtils.transformBlockEntityNbt(PacketContext.get(), packet.getBlockEntityType(), packet.getNbt());
            if (packet.getNbt() == nbt) {
                return packet;
            }
            return BlockEntityUpdateS2CPacketAccessor.createBlockEntityUpdateS2CPacket(packet.getPos(), packet.getBlockEntityType(), nbt);
        });
    }
}
