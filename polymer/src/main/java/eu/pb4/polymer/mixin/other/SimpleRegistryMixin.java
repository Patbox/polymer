package eu.pb4.polymer.mixin.other;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.impl.other.DeferredRegistryEntry;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends Registry<T> implements RegistryExtension<T> {
    @Shadow public abstract RegistryEntry<T> add(RegistryKey<T> key, T entry, Lifecycle lifecycle);

    @Nullable
    @Unique
    private List<T> polymer_objects = null;

    @Unique
    private List<DeferredRegistryEntry<T>> polymer_deferredRegistration = new ArrayList<>();

    @Unique
    private boolean polymer_deferRegistration = true;

    protected SimpleRegistryMixin(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    /*@Inject(method = "add", at = @At("HEAD"), cancellable = true)
    private <V extends T> void polymer_deferRegistration(RegistryKey<T> key, T entry, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        if (entry instanceof PolymerBlock && this.polymer_deferRegistration) {
            this.polymer_deferredRegistration.add(new DeferredRegistryEntry<>(key, entry, lifecycle));
            cir.setReturnValue((RegistryEntry<T>) ((Block) entry).getRegistryEntry());
        }
    }*/

    @Inject(method = "freeze", at = @At("HEAD"))
    private void polymer_registerDeferred(CallbackInfoReturnable<Registry<T>> cir) {
        this.polymer_deferRegistration = false;
        for (var obj : this.polymer_deferredRegistration) {
            this.add(obj.registryKey(), obj.entry(), obj.lifecycle());
        }
        this.polymer_deferredRegistration.clear();
    }

    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/util/registry/RegistryEntry;", at = @At("TAIL"))
    private <V extends T> void polymer_storeStatus(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.polymer_objects = null;
        if (PolymerObject.is(entry)) {
            RegistrySyncUtils.setServerEntry(this, entry);
        }

        PolymerImplUtils.invokeRegistered((Registry<Object>) this, entry);
    }

    @Override
    public List<T> polymer_getEntries() {
        if (this.polymer_objects == null) {
            this.polymer_objects = new ArrayList<>();
            for (var obj : this) {
                if (PolymerUtils.isServerOnly(obj)) {
                    this.polymer_objects.add(obj);
                }
            }
        }

        return this.polymer_objects;
    }
}
