package eu.pb4.polymer.core.impl.interfaces;

import java.util.function.IntFunction;
import net.minecraft.core.IdMap;

public interface IndexedNetwork<T> extends IdMap<T> {

    void polymer$setDecoder(IntFunction<T> decoder);

    static <T> void set(IdMap<T> i, IntFunction<T> decoder) {
        ((IndexedNetwork<T>) i).polymer$setDecoder(decoder);
    }
}
