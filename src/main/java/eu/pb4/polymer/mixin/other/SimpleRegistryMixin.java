package eu.pb4.polymer.mixin.other;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.interfaces.VirtualObject;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> {
    @Shadow private int nextId;

    @Shadow public abstract <V extends T> V set(int rawId, RegistryKey<T> key, V entry, Lifecycle lifecycle);

    @Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private <V extends T> void polymer_moveVirtualObjectToTheEnd(RegistryKey<T> key, V entry, Lifecycle lifecycle, CallbackInfoReturnable<V> cir) {
        if (entry instanceof VirtualObject) {
            int current = this.nextId;
            this.set(current + 1000000, key, entry, lifecycle);
            this.nextId = current + 1;
            cir.setReturnValue(entry);
        }
    }
}
