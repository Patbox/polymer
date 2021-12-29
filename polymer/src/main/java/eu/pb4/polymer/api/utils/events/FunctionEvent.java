package eu.pb4.polymer.api.utils.events;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FunctionEvent<T, R> {
    private List<T> handlers = new ArrayList<>();

    public void register(T listener) {
        this.handlers.add(listener);
    }

    public R invoke(Function<Collection<T>, R> invoker) {
        return invoker.apply(handlers);
    }
}
