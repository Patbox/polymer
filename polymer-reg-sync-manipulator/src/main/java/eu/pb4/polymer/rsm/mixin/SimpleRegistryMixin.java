package eu.pb4.polymer.rsm.mixin;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RegistrySyncExtension<T>, MutableRegistry<T> {
    @Unique
    private final Object2BooleanMap<T> polymer_registry_sync$entryStatus = new Object2BooleanOpenHashMap<>();

    @Shadow public abstract Set<Identifier> getIds();

    @Unique
    private Status polymer_registry_sync$status = null;

    @Inject(method = "set", at = @At("TAIL"))
    private <V extends T> void polymer_registry_sync$resetStatus(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.polymer_registry_sync$status = null;
    }

    @Override
    public Status polymer_registry_sync$getStatus() {
        if (this.polymer_registry_sync$status == null) {
            var status = Status.VANILLA;
            for (var id : this.getIds()) {
                if (id.getNamespace().equals("minecraft") || id.getNamespace().equals("brigadier")) {
                    continue;
                }

                if (RegistrySyncUtils.isServerEntry(this, id)) {
                    status = Status.WITH_SERVER_ONLY;
                } else {
                    status = Status.WITH_MODDED;
                    break;
                }
            }

            this.polymer_registry_sync$status = status;
        }

        return this.polymer_registry_sync$status;
    }

    @Override
    public void polymer_registry_sync$setStatus(Status status) {
        this.polymer_registry_sync$status = status;
    }

    @Override
    public void polymer_registry_sync$clearStatus() {
        this.polymer_registry_sync$status = null;
    }

    @Override
    public boolean polymer_registry_sync$isServerEntry(T obj) {
        return this.polymer_registry_sync$entryStatus.getBoolean(obj);
    }

    @Override
    public void polymer_registry_sync$setServerEntry(T obj, boolean value) {
        this.polymer_registry_sync$status = null;
        this.polymer_registry_sync$entryStatus.put(obj, value);
    }
}
