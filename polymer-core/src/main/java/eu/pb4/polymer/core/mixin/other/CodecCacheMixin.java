package eu.pb4.polymer.core.mixin.other;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/util/dynamic/CodecCache$2")
public class CodecCacheMixin {
    @Shadow
    @Final
    Codec<Object> field_51505;
    @Inject(method = "encode", at = @At("HEAD"), cancellable = true)
    private <T> void dontCacheOnNetworking(Object value, DynamicOps<T> ops, T prefix, CallbackInfoReturnable<DataResult<T>> cir) {
        if (PolymerCommonUtils.isServerNetworkingThread()) {
            cir.setReturnValue(this.field_51505.encode(value, ops, prefix));
        }
    }
}