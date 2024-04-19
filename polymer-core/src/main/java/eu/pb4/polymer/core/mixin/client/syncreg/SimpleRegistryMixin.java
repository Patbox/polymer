package eu.pb4.polymer.core.mixin.client.syncreg;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.interfaces.IndexedNetwork;
import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.collection.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.function.IntFunction;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements IndexedNetwork<T>, Registry<T> {
    @Unique
    private IntFunction<T> polymer$decoder;
    @Unique
    private boolean hasDecoder;

    @Inject(method = "get(I)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
    private void redirectGets(int i, CallbackInfoReturnable<T> cir) {
        if (this.hasDecoder && InternalClientRegistry.enabled) {
            var x = this.polymer$decoder.apply(i);

            if (x != null) {
                cir.setReturnValue(x);
            }
        }
    }

    @Inject(method = "getEntry(I)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private void redirectGets2(int i, CallbackInfoReturnable<Optional<RegistryEntry<T>>> cir) {
        if (this.hasDecoder && InternalClientRegistry.enabled) {
            var x = this.polymer$decoder.apply(i);

            if (x != null) {
                cir.setReturnValue(Optional.of(this.getEntry(x)));
            }
        }
    }

    @Override
    public void polymer$setDecoder(IntFunction<T> decoder) {
        this.polymer$decoder = decoder;
        this.hasDecoder = true;
    }
}
