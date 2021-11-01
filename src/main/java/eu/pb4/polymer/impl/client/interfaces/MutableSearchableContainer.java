package eu.pb4.polymer.impl.client.interfaces;

import java.util.function.Predicate;

public interface MutableSearchableContainer {
    void polymer_remove(Object obj);
    void polymer_removeIf(Predicate<Object> predicate);
}
