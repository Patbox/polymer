package eu.pb4.polymer.networking.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtCustomPayloadCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundCustomPayloadPacket.class)
public class ServerboundCustomPayloadPacketMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;codec(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$FallbackProvider;Ljava/util/List;)Lnet/minecraft/network/codec/StreamCodec;"))
    private static StreamCodec modifyCodec(StreamCodec codec) {
        ((ExtCustomPayloadCodec) codec).polymer$setCodecMap(ClientPackets.PAYLOAD_CODEC);
        return codec;
    }
}
