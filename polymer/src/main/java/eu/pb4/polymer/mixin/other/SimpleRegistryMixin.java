package eu.pb4.polymer.mixin.other;

import com.mojang.serialization.Lifecycle;
import eu.pb4.polymer.api.utils.PolymerObject;
import eu.pb4.polymer.api.utils.PolymerUtils;
import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.interfaces.RegistryExtension;
import eu.pb4.polymer.rsm.api.RegistrySyncUtils;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RegistryExtension<T>, Registry<T> {
    @Shadow private volatile Map<TagKey<T>, RegistryEntryList.Named<T>> tagToEntryList;

    @Shadow public abstract RegistryEntry.Reference<T> add(RegistryKey<T> key, T entry, Lifecycle lifecycle);

    @Nullable
    @Unique
    private List<T> polymer$objects = null;

    @Inject(method = "set", at = @At("TAIL"))
    private <V extends T> void polymer$storeStatus(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, CallbackInfoReturnable<RegistryEntry<T>> cir) {
        this.polymer$objects = null;
        if (PolymerObject.is(value)) {
            RegistrySyncUtils.setServerEntry(this, value);
        }

        PolymerImplUtils.invokeRegistered((Registry<Object>) this, value);
    }

    @Override
    public List<T> polymer$getEntries() {
        if (this.polymer$objects == null) {
            this.polymer$objects = new ArrayList<>();
            for (var obj : this) {
                if (PolymerUtils.isServerOnly(obj)) {
                    this.polymer$objects.add(obj);
                }
            }
        }

        return this.polymer$objects;
    }

    @Override
    public Map<TagKey<T>, RegistryEntryList.Named<T>> polymer$getTagsInternal() {
        return this.tagToEntryList;
    }
}
