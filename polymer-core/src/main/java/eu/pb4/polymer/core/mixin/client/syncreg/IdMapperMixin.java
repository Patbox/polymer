package eu.pb4.polymer.core.mixin.client.syncreg;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.IntFunction;
import net.minecraft.core.IdMapper;

@Mixin(IdMapper.class)
public abstract class IdMapperMixin<T> implements IndexedNetwork<T> {
    @Unique
    private IntFunction<T> polymer$decoder = _ -> null;
    @Unique
    private boolean hasDecoder;

    @Inject(method = "byId(I)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void redirectGets(int i, CallbackInfoReturnable<T> cir) {
        if (this.hasDecoder && InternalClientRegistry.enabled && PolymerCommonUtils.isClientNetworkingThread()) {
            var x = this.polymer$decoder.apply(i);

            if (x != null) {
                cir.setReturnValue(x);
            }
        }
    }

    @Override
    public void polymer$setDecoder(IntFunction<T> decoder) {
        this.polymer$decoder = decoder;
        this.hasDecoder = true;
    }

    @Override
    public IntFunction<T> polymer$getDecoder() {
        return this.polymer$decoder;
    }
}
