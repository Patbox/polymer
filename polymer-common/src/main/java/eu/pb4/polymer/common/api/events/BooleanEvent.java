package eu.pb4.polymer.common.api.events;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class BooleanEvent<T> {
    private final List<T> handlers = new ArrayList<>();

    public void register(T listener) {
        this.handlers.add(listener);
    }

    public T registerRet(T listener) {
        this.handlers.add(listener);
        return listener;
    }

    public void unregister(T listener) {
        this.handlers.remove(listener);
    }

    public boolean invoke(Predicate<T> invoker) {
        for (var handler : handlers) {
            var bool = invoker.test(handler);

            if (bool) {
                return true;
            }
        }
        return false;
    }

    public Collection<T> invokers() {
        return Collections.unmodifiableCollection(this.handlers);
    }
}
