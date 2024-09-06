package eu.pb4.polymer.common.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public record FakeRegistry<T>(RegistryKey<? extends Registry<T>> registryKey, Identifier defaultId, T defaultValue) implements Registry<T>, RegistryEntryOwner<T> {

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return registryKey;
    }

    @Nullable
    @Override
    public Identifier getId(T value) {
        return defaultId;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return Optional.empty();
    }

    @Override
    public int getRawId(@Nullable T value) {
        return 0;
    }

    @Nullable
    @Override
    public T get(int index) {
        return defaultValue;
    }

    @Override
    public int size() {
        return 1;
    }

    @Nullable
    @Override
    public T get(@Nullable RegistryKey<T> key) {
        return defaultValue;
    }

    @Nullable
    @Override
    public T get(@Nullable Identifier id) {
        return defaultValue;
    }

    @Override
    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> key) {
        return Optional.empty();
    }

    @Override
    public Lifecycle getLifecycle() {
        return Lifecycle.experimental();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getDefaultEntry() {
        return Optional.empty();
    }

    @Override
    public Set<Identifier> getIds() {
        return Set.of(defaultId);
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Set.of();
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return Set.of();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return Optional.empty();
    }

    @Override
    public boolean containsId(Identifier id) {
        return true;
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return true;
    }

    @Override
    public Registry<T> freeze() {
        return this;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        return RegistryEntry.Reference.intrusive(this, value);
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        return Optional.empty();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier id) {
        return Optional.of(RegistryEntry.Reference.intrusive(this, this.defaultValue));
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        return RegistryEntry.of(value);
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return null;
    }

    @Override
    public Stream<RegistryEntryList.Named<T>> getTags() {
        return Stream.empty();
    }

    @Override
    public Stream<RegistryEntryList.Named<T>> streamTags() {
        return null;
    }

    @Override
    public PendingTagLoad<T> startTagReload(TagGroupLoader.RegistryTags<T> tags) {
        return null;
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
    public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
        return getEntry(key.getValue());
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
        return Optional.empty();
    }
}
