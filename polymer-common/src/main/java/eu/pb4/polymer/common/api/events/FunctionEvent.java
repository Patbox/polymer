package eu.pb4.polymer.common.api.events;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public final class FunctionEvent<T, R> {
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

    public R invoke(Function<Collection<T>, R> invoker) {
        return invoker.apply(handlers);
    }

    public Collection<T> invokers() {
        return Collections.unmodifiableCollection(this.handlers);
    }
}
