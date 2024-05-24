package eu.pb4.polymer.rsm.mixin;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Set;

@Mixin(value = SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RegistrySyncExtension<T>, MutableRegistry<T> {
    @Unique
    private final Reference2BooleanOpenHashMap<T> polymer_registry_sync$entryStatus = new Reference2BooleanOpenHashMap<>();
    @Shadow public abstract Set<Identifier> getIds();

    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow protected abstract void assertNotFrozen();
    @Unique
    private Status registryStatus = null;
    @Unique
    private boolean alreadyOrdered = false;

    @Inject(method = "add", at = @At("TAIL"))
    private <V extends T> void resetStatusOnAdd(RegistryKey<T> key, T value, RegistryEntryInfo info, CallbackInfoReturnable<RegistryEntry.Reference<T>> cir) {
        this.registryStatus = null;
        this.alreadyOrdered = false;
    }

    @Inject(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private void reorderOnFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        polymer_registry_sync$reorderEntries();
    }

    @Override
    public void polymer_registry_sync$reorderEntries() {
        if (this.polymer_registry_sync$entryStatus.isEmpty() || alreadyOrdered) {
            return;
        }

        var vanilla = new ArrayList<RegistryEntry.Reference<T>>();
        var polymer = new ArrayList<RegistryEntry.Reference<T>>();

        for (var entry : this.rawIdToEntry) {
            if (this.polymer_registry_sync$isServerEntry(entry.value())) {
                polymer.add(entry);
            } else {
                vanilla.add(entry);
            }
        }

        this.rawIdToEntry.clear();
        this.rawIdToEntry.addAll(vanilla);
        this.rawIdToEntry.addAll(polymer);
        this.entryToRawId.clear();

        for (var i = 0; i < this.rawIdToEntry.size(); i++) {
            this.entryToRawId.put(this.rawIdToEntry.get(i).value(), i);
        }
        this.alreadyOrdered = true;
    }

    @Override
    public Status polymer_registry_sync$getStatus() {
        if (this.registryStatus == null) {
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

            this.registryStatus = status;
        }

        return this.registryStatus;
    }

    @Override
    public void polymer_registry_sync$setStatus(Status status) {
        this.registryStatus = status;
    }

    @Override
    public void polymer_registry_sync$clearStatus() {
        this.registryStatus = null;
    }

    @Override
    public boolean polymer_registry_sync$isServerEntry(T obj) {
        return this.polymer_registry_sync$entryStatus.getBoolean(obj);
    }

    @Override
    public void polymer_registry_sync$setServerEntry(T obj, boolean value) {
        this.assertNotFrozen();
        this.registryStatus = null;
        this.polymer_registry_sync$entryStatus.put(obj, value);
    }
}
