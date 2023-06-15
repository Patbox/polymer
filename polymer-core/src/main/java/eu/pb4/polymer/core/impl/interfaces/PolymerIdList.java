package eu.pb4.polymer.core.impl.interfaces;

import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

@ApiStatus.Internal
public interface PolymerIdList<T> {
    void polymer$setChecker(Predicate<T> polymerChecker, Predicate<T> serverChecker, Function<T, String> name);
    void polymer$setIgnoreCalls(boolean value);
    Collection<T> polymer$getPolymerEntries();
    int polymer$getOffset();
    void polymer$clear();

    int polymer$getVanillaBitCount();

    void polymer$setReorderLock(boolean value);
    boolean polymer$getReorderLock();
}
