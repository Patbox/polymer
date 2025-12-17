package eu.pb4.polymer.core.impl.networking;

import java.util.function.BiFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record TransformingPacketCodec<B, V>(StreamCodec<B, V> codec, BiFunction<B, V, V> encodeTransform, BiFunction<B, V, V> decodeTransform) implements StreamCodec<B, V> {
    private static final BiFunction<FriendlyByteBuf, Object, Object> PASSTHROUGH = (b, o) -> o;

    @Override
    public V decode(B buf) {
        return decodeTransform.apply(buf, this.codec.decode(buf));
    }

    @Override
    public void encode(B buf, V value) {
        this.codec.encode(buf, this.encodeTransform.apply(buf, value));
    }

    public static <B, V> StreamCodec<B, V> encodeOnly(StreamCodec<B, V> codec, BiFunction<B, V, V> encodeTransform) {
        //noinspection unchecked
        return new TransformingPacketCodec<>(codec, encodeTransform, (BiFunction<B, V, V>) PASSTHROUGH);
    }
}
