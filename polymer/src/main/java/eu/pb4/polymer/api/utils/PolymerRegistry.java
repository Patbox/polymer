package eu.pb4.polymer.api.utils;

import net.minecraft.util.Identifier;

import java.util.Map;

public interface PolymerRegistry<T> extends Iterable<T> {
    T get(Identifier identifier);
    T get(int id);
    Identifier getId(T entry);
    int getRawId(T entry);
    Iterable<Identifier> ids();
    Iterable<Map.Entry<Identifier, T>> entries();
    int size();
}
