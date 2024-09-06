package eu.pb4.polymer.common.api.events;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class SimpleEvent<T> {
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

    public void invoke(Consumer<T> invoker) {
        for (var handler : handlers) {
            invoker.accept(handler);
        }
    }

    public boolean isEmpty() {
        return this.handlers.isEmpty();
    }

    public Collection<T> invokers() {
        return Collections.unmodifiableCollection(this.handlers);
    }
}
