package eu.pb4.polymer.api.utils.events;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BooleanEvent<T> {
    private List<T> handlers = new ArrayList<>();

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

            if (bool == true) {
                return true;
            }
        }
        return false;
    }
}
