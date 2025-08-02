package eu.pb4.polymer.common.impl;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.BaseMapCodec;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public record SortedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec, Comparator<Map.Entry<K, V>> comparator) implements Codec<Map<K, V>>, BaseMapCodec<K, V> {
    public static <K extends Comparable<K>, V> SortedMapCodec<K, V> of(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new SortedMapCodec<>(keyCodec, elementCodec, Map.Entry.comparingByKey());
    }

    public static <K, V> SortedMapCodec<K, V> of(Codec<K> keyCodec, Codec<V> elementCodec, Comparator<K> comparator) {
        return new SortedMapCodec<>(keyCodec, elementCodec, Map.Entry.comparingByKey(comparator));
    }

    @Override
    public <T> DataResult<Pair<Map<K, V>, T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map)).map(r -> Pair.of(r, input));
    }

    @Override
    public <T> DataResult<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final T prefix) {
        return encode(input, ops, ops.mapBuilder()).build(prefix);
    }

    @Override
    public <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        var entries = new ArrayList<>(input.entrySet());
        entries.sort(this.comparator);

        for (final var entry : entries) {
            prefix.add(keyCodec.encodeStart(ops, entry.getKey()), elementCodec.encodeStart(ops, entry.getValue()));
        }
        return prefix;
    }

    @Override
    public String toString() {
        return "SortedMapCodec[" + keyCodec + " -> " + elementCodec + ']';
    }
}
