package eu.pb4.polymer.mixin.other;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.impl.interfaces.RegistryExtension;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SimpleRegistry.class)
public class SimpleRegistryMixin<T> implements RegistryExtension {
    @Unique
    private Status polymer_status = Status.VANILLA_ONLY;
    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/util/registry/RegistryEntry;", at = @At("TAIL"))
    private <V extends T> void polymer_storeStatus(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        if (key.getValue().getNamespace().equals("minecraft")) {
            return;
        }

        if (entry instanceof PolymerObject && Status.WITH_POLYMER.canReplace(this.polymer_status)) {
            this.polymer_status = Status.WITH_POLYMER;
        } else {
            this.polymer_status = Status.WITH_REGULAR_MODS;
        }
    }

    @Override
    public Status polymer_getStatus() {
        return this.polymer_status;
    }

    @Override
    public void polymer_setStatus(Status status) {
        this.polymer_status = status;
    }

    @Override
    public boolean polymer_updateStatus(Status status) {
        if (status.canReplace(this.polymer_status)) {
            this.polymer_status = status;
            return true;
        }

        return false;
    }
}
