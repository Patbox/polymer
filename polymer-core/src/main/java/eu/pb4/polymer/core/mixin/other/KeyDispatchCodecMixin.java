package eu.pb4.polymer.core.mixin.other;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = KeyDispatchCodec.class, remap = false)
public class KeyDispatchCodecMixin {
    @ModifyArg(method = "encode", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1))
    private Object replaceWithObject(Object object, @Local MapEncoder<Object> elementEncoder) {
        return elementEncoder instanceof PolymerMapCodec<?> codec
                && PolymerCommonUtils.isServerNetworkingThreadWithContext() ? codec.fallbackValue() : object;
    }
    @ModifyArg(method = "encode", at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 2))
    private Object replaceWithObject2(Object object, @Local MapEncoder<Object> elementEncoder) {
        return elementEncoder instanceof PolymerMapCodec<?> codec
                && PolymerCommonUtils.isServerNetworkingThreadWithContext() ? codec.fallbackValue() : object;
    }
}
