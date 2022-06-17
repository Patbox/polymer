package eu.pb4.polymer.rsm.mixin;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;
import java.util.Set;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> extends Registry<T> implements RegistrySyncExtension<T> {
    private Object2BooleanMap<T> polymerRSM_entryStatus = new Object2BooleanOpenHashMap<>();

    @Shadow public abstract Set<Identifier> getIds();

    @Unique
    private Status polymerRSM_status = null;

    protected SimpleRegistryMixin(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    @Inject(method = "set(ILnet/minecraft/util/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;Z)Lnet/minecraft/util/registry/RegistryEntry;", at = @At("TAIL"))
    private <V extends T> void polymerRSM_resetStatus(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle, boolean checkDuplicateKeys, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.polymerRSM_status = null;
    }

    @Inject(method = "replace", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private <V extends T> void polymerRSM_removeOldEntry(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir, RegistryEntry<T> registryEntry, T object) {
        this.polymerRSM_entryStatus.removeBoolean(object);
    }

    @Override
    public Status polymerRSM_getStatus() {
        if (this.polymerRSM_status == null) {
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

            this.polymerRSM_status = status;
        }

        return this.polymerRSM_status;
    }

    @Override
    public void polymerRSM_setStatus(Status status) {
        this.polymerRSM_status = status;
    }

    @Override
    public void polymerRSM_clearStatus() {
        this.polymerRSM_status = null;
    }

    @Override
    public boolean polymerRSM_isServerEntry(T obj) {
        return this.polymerRSM_entryStatus.getBoolean(obj);
    }

    @Override
    public void polymerRSM_setServerEntry(T obj, boolean value) {
        this.polymerRSM_status = null;
        this.polymerRSM_entryStatus.put(obj, value);
    }
}
