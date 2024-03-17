package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.impl.ExtCustomPayloadCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(targets = "net/minecraft/network/packet/CustomPayload$1")
public class CustomPayloadCodecMixin implements ExtCustomPayloadCodec {
    @Unique
    private Map<Identifier, PacketCodec<ByteBuf, ?>> codecs = Map.of();
    @Inject(method = "getCodec", at = @At("HEAD"), cancellable = true)
    private void supportCustomPayloads(Identifier id, CallbackInfoReturnable<PacketCodec<PacketByteBuf, ?>> cir) {
        var x = codecs.get(id);
        if (x != null) {
            cir.setReturnValue(x.cast());
        }
    }

    @Override
    public void polymer$setCodecMap(Map<Identifier, PacketCodec<ByteBuf, ?>> codecs) {
        this.codecs = codecs;
    }
}
