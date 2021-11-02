package eu.pb4.polymer.api.utils;

import net.minecraft.util.Identifier;

public interface PolymerRegistry<T> extends Iterable<T> {
    T get(Identifier identifier);
    T get(int id);
    Identifier getId(T entry);
    int getRawId(T entry);

    int size();
}
