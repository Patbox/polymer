package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.api.utils.PolymerObject;
import eu.pb4.polymer.core.api.utils.PolymerSyncedObject;
import eu.pb4.polymer.core.impl.ImplPolymerRegistryEvent;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import eu.pb4.polymer.core.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements RegistryExtension<T>, Registry<T> {
    @Shadow @Final private Map<TagKey<T>, HolderSet.Named<T>> frozenTags;
    @Nullable
    @Unique
    private List<T> polymer$objects = null;
    @Unique
    private final IdentityHashMap<T, PolymerSyncedObject<T>> overlays = new IdentityHashMap<>();

    @Inject(method = "register(Lnet/minecraft/resources/ResourceKey;Ljava/lang/Object;Lnet/minecraft/core/RegistrationInfo;)Lnet/minecraft/core/Holder$Reference;", at = @At("TAIL"))
    private <V extends T> void polymer$storeStatus(ResourceKey<T> key, T value, RegistrationInfo info, CallbackInfoReturnable<Holder.Reference<T>> cir) {
        this.polymer$objects = null;
        if (PolymerObject.is(value)) {
            RegistrySyncUtils.setServerEntry(this, value);
        }

        ImplPolymerRegistryEvent.invokeRegistered(this, value);
    }

    @Override
    public List<T> polymer$getEntries() {
        if (this.polymer$objects == null) {
            this.polymer$objects = new ArrayList<>();
            for (var obj : this) {
                if (PolymerImplUtils.isServerSideSyncableEntry(this, obj)) {
                    this.polymer$objects.add(obj);
                }
            }
        }

        return this.polymer$objects;
    }

    @Override
    public Map<TagKey<T>, HolderSet.Named<T>> polymer$getTagsInternal() {
        return this.frozenTags;
    }

    @Override
    public void polymer$setOverlay(T value, PolymerSyncedObject<T> syncedObject) {
        this.overlays.put(value, syncedObject);
    }

    @Override
    public PolymerSyncedObject<T> polymer$getOverlay(T value) {
        return this.overlays.get(value);
    }
}
