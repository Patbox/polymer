package eu.pb4.polymer.core.mixin.client.syncreg;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.IntFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements IndexedNetwork<T>, Registry<T> {
    @Unique
    private IntFunction<T> polymer$decoder = _ -> null;
    @Unique
    private boolean hasDecoder;

    @Inject(method = "byId(I)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void redirectGets(int i, CallbackInfoReturnable<T> cir) {
        if (this.hasDecoder && InternalClientRegistry.enabled) {
            var x = this.polymer$decoder.apply(i);

            if (x != null) {
                cir.setReturnValue(x);
            }
        }
    }

    @Inject(method = "get(I)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void redirectGets2(int i, CallbackInfoReturnable<Optional<Holder<T>>> cir) {
        if (this.hasDecoder && InternalClientRegistry.enabled) {
            var x = this.polymer$decoder.apply(i);

            if (x != null) {
                cir.setReturnValue(Optional.of(this.wrapAsHolder(x)));
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
