package eu.pb4.polymer.common.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.component.DataComponentLookup;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.RandomSource;

public record FakeRegistry<T>(ResourceKey<? extends Registry<T>> registryKey, Identifier defaultId, T defaultValue) implements Registry<T>, HolderOwner<T> {

    @Override
    public ResourceKey<? extends Registry<T>> key() {
        return registryKey;
    }

    @Nullable
    @Override
    public Identifier getKey(T value) {
        return defaultId;
    }

    @Override
    public Optional<ResourceKey<T>> getResourceKey(T entry) {
        return Optional.of(ResourceKey.create(registryKey, defaultId));
    }

    @Override
    public int getId(@Nullable T value) {
        return 0;
    }

    @Nullable
    @Override
    public T byId(int index) {
        return defaultValue;
    }

    @Override
    public int size() {
        return 1;
    }

    @Nullable
    @Override
    public T getValue(@Nullable ResourceKey<T> key) {
        return defaultValue;
    }

    @Nullable
    @Override
    public T getValue(@Nullable Identifier id) {
        return defaultValue;
    }

    @Override
    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> key) {
        return Optional.of(RegistrationInfo.BUILT_IN);
    }

    @Override
    public Lifecycle registryLifecycle() {
        return Lifecycle.experimental();
    }

    @Override
    public Optional<Holder.Reference<T>> getAny() {
        return Optional.of(createIntrusiveHolder(defaultValue));
    }

    @Override
    public Set<Identifier> keySet() {
        return Set.of(defaultId);
    }

    @Override
    public Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return Set.of();
    }

    @Override
    public Set<ResourceKey<T>> registryKeySet() {
        return Set.of();
    }

    @Override
    public Optional<Holder.Reference<T>> getRandom(RandomSource random) {
        return Optional.empty();
    }

    @Override
    public boolean containsKey(Identifier id) {
        return true;
    }

    @Override
    public boolean containsKey(ResourceKey<T> key) {
        return true;
    }

    @Override
    public Registry<T> freeze() {
        return this;
    }

    @Override
    public Holder.Reference<T> createIntrusiveHolder(T value) {
        return Holder.Reference.createIntrusive(this, value);
    }

    @Override
    public Optional<Holder.Reference<T>> get(int rawId) {
        return getAny();
    }

    @Override
    public Optional<Holder.Reference<T>> get(Identifier id) {
        return getAny();
    }

    @Override
    public Holder<T> wrapAsHolder(T value) {
        return Holder.direct(value);
    }

    @Override
    public Stream<Holder.Reference<T>> listElements() {
        return Stream.empty();
    }

    @Override
    public Stream<HolderSet.Named<T>> listTags() {
        return Stream.empty();
    }

    @Override
    public Stream<HolderSet.Named<T>> getTags() {
        return Stream.empty();
    }

    @Override
    public PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> tags) {
        return null;
    }

    @Override
    public DataComponentLookup<T> componentLookup() {
        return new DataComponentLookup<>(List.of());
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public T next() {
                return null;
            }
        };
    }

    @Override
    public Optional<Holder.Reference<T>> get(ResourceKey<T> key) {
        return get(key.identifier());
    }

    @Override
    public Optional<HolderSet.Named<T>> get(TagKey<T> tag) {
        return Optional.empty();
    }
}
