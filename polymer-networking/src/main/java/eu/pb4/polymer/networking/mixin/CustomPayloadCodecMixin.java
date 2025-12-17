package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.impl.ExtCustomPayloadCodec;
import io.netty.buffer.ByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

@Mixin(targets = "net/minecraft/network/protocol/common/custom/CustomPacketPayload$1")
public class CustomPayloadCodecMixin implements ExtCustomPayloadCodec {
    @Unique
    private Map<Identifier, StreamCodec<ByteBuf, ?>> codecs = Map.of();
    @Inject(method = "findCodec", at = @At("HEAD"), cancellable = true)
    private void supportCustomPayloads(Identifier id, CallbackInfoReturnable<StreamCodec<FriendlyByteBuf, ?>> cir) {
        var x = codecs.get(id);
        if (x != null) {
            cir.setReturnValue(x.cast());
        }
    }

    @Override
    public void polymer$setCodecMap(Map<Identifier, StreamCodec<ByteBuf, ?>> codecs) {
        this.codecs = codecs;
    }
}
