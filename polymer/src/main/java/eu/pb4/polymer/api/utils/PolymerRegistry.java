package eu.pb4.polymer.api.utils;

import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;

import java.util.Map;

public interface PolymerRegistry<T> extends IndexedIterable<T> {
    T get(Identifier identifier);
    T get(int id);
    Identifier getId(T entry);
    int getRawId(T entry);
    Iterable<Identifier> ids();
    Iterable<Map.Entry<Identifier, T>> entries();
    int size();
}
