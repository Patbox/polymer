package eu.pb4.polymer.rsm.mixin;

import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import eu.pb4.polymer.rsm.impl.RegSyncImplUtil;
import eu.pb4.polymer.rsm.impl.RegistrySyncExtension;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Set;

@Mixin(value = MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RegistrySyncExtension<T>, WritableRegistry<T> {
    @Unique
    private final Reference2BooleanOpenHashMap<T> polymer_registry_sync$entryStatus = new Reference2BooleanOpenHashMap<>();
    @Shadow public abstract Set<Identifier> keySet();

    @Shadow @Final private ObjectList<Holder.Reference<T>> byId;
    @Shadow @Final private Reference2IntMap<T> toId;
    @Shadow protected abstract void validateWrite();
    @Unique
    private Status registryStatus = null;
    @Unique
    private boolean alreadyOrdered = false;

    @Inject(method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;", at = @At("TAIL"))
    private <V extends T> void resetStatusOnAdd(ResourceKey<T> key, T value, RegistrationInfo info, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        this.registryStatus = null;
        this.alreadyOrdered = false;
    }

    @Inject(method = "freeze", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private void reorderOnFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        this.polymer_registry_sync$reorderEntries();
    }

    @Override
    public void polymer_registry_sync$reorderEntries() {
        if (this.polymer_registry_sync$entryStatus.isEmpty() || alreadyOrdered) {
            return;
        }

        var vanilla = new ArrayList<Holder.Reference<T>>();
        var polymer = new ArrayList<Holder.Reference<T>>();

        for (var entry : this.byId) {
            if (this.polymer_registry_sync$isServerEntry(entry.value())) {
                polymer.add(entry);
            } else {
                vanilla.add(entry);
            }
        }

        this.byId.clear();
        this.byId.addAll(vanilla);
        this.byId.addAll(polymer);
        this.toId.clear();

        for (var i = 0; i < this.byId.size(); i++) {
            this.toId.put(this.byId.get(i).value(), i);
        }
        this.alreadyOrdered = true;
    }

    @Override
    public Status polymer_registry_sync$getStatus() {
        if (this.registryStatus == null) {
            var status = Status.VANILLA;
            for (var id : this.keySet()) {
                if (RegSyncImplUtil.isVanillaId(id)) {
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
        this.validateWrite();
        this.registryStatus = null;
        this.polymer_registry_sync$entryStatus.put(obj, value);
    }
}
