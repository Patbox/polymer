package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.networking.impl.ExtCustomPayloadCodec;
import eu.pb4.polymer.networking.impl.ServerPackets;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec modifyCodec(PacketCodec codec) {
        ((ExtCustomPayloadCodec) codec).polymer$setCodecMap(ServerPackets.PAYLOAD_CODEC);
        return codec;
    }
}
