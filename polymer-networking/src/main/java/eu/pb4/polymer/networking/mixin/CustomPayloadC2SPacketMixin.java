package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtCustomPayloadCodec;
import eu.pb4.polymer.networking.impl.ServerPackets;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec modifyCodec(PacketCodec codec) {
        ((ExtCustomPayloadCodec) codec).polymer$setCodecMap(ClientPackets.PAYLOAD_CODEC);
        return codec;
    }
}
