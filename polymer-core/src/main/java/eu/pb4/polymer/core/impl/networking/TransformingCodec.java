package eu.pb4.polymer.core.impl.networking;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.function.BiFunction;
import java.util.stream.Stream;

public class TransformingCodec<T> implements Codec<T> {
    final Codec<T> codec;
    final BiFunction<T, DynamicOps<?>, T> encodeTransform;
    final BiFunction<T, DynamicOps<?>, T> decodeTransform;

    public TransformingCodec(Codec<T> codec, BiFunction<T, DynamicOps<?>, T> encodeTransform, BiFunction<T, DynamicOps<?>, T> decodeTransform) {
        this.codec = codec;
        this.encodeTransform = encodeTransform;
        this.decodeTransform = decodeTransform;
    }

    public static <T> Codec<T> encodeOnly(Codec<T> codec, BiFunction<T, DynamicOps<?>, T> encodeTransform) {

        return new TransformingCodec<>(codec, encodeTransform, null) {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
                return this.codec.decode(ops, input);
            }
        };
    }


    @Override
    public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
        return this.codec.decode(ops, input).map(x -> x.mapFirst(y -> this.decodeTransform.apply(y, ops)));
    }

    @Override
    public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
        return this.codec.encode(this.encodeTransform.apply(input, ops), ops, prefix);
    }
}
