package eu.pb4.polymer.rsm.mixin;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RegistrySyncExtension<T>, MutableRegistry<T> {
    private Object2BooleanMap<T> polymerRSM_entryStatus = new Object2BooleanOpenHashMap<>();

    @Shadow public abstract Set<Identifier> getIds();

    @Unique
    private Status polymerRSM_status = null;

    @Inject(method = "set", at = @At("TAIL"))
    private <V extends T> void polymerRSM_resetStatus(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.polymerRSM_status = null;
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
