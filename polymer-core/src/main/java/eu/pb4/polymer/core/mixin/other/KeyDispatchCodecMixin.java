package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Function;

@Mixin(value = KeyDispatchCodec.class, remap = false)
public class KeyDispatchCodecMixin {
    @Shadow @Final private Function<Object, ? extends DataResult<? extends MapEncoder<Object>>> encoder;

    @ModifyVariable(method = "encode", at = @At("STORE"), ordinal = 0)
    private DataResult<? extends MapEncoder<Object>> replaceWithObject(DataResult<? extends MapEncoder<Object>> encoderResult, @Local(argsOnly = true) LocalRef<Object> object) {
        if (encoderResult.isError()) {
            return encoderResult;
        }

        if (PolymerMapCodec.getOverlay(encoderResult.getOrThrow()) instanceof PolymerMapCodec<Object> codec && PolymerCommonUtils.isServerNetworkingThread()) {
            var rep = codec.getPolymerReplacement(object.get(), PacketContext.get());
            object.set(rep);
            return encoder.apply(rep);
        }

        return encoderResult;
    }
}
