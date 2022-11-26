package eu.pb4.polymer.core.impl.interfaces;

import net.minecraft.util.collection.IndexedIterable;

import java.util.function.IntFunction;

public interface IndexedNetwork<T> extends IndexedIterable<T> {

    IntFunction<T> polymer$getDecoder();
    void polymer$setDecoder(IntFunction<T> decoder);

    static <T> void set(IndexedIterable<T> i, IntFunction<T> decoder) {
        ((IndexedNetwork<T>) i).polymer$setDecoder(decoder);
    }
}
