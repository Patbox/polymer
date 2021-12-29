package eu.pb4.polymer.api.utils.events;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class BooleanEvent<T> {
    private List<T> handlers = new ArrayList<>();

    public void register(T listener) {
        this.handlers.add(listener);
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
