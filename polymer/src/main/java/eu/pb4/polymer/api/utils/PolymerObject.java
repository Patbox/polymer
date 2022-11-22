package eu.pb4.polymer.api.utils;

import java.util.function.Predicate;

/**
 * Used to mark general polymer objects
 */
public interface PolymerObject {
    Predicate<Object> PREDICATE = (obj) -> obj instanceof PolymerObject;
    Predicate<Object> PREDICATE_NOT = (obj) -> !(obj instanceof PolymerObject);

    static boolean is(Object obj) {
        return obj instanceof PolymerObject;
    }


}
