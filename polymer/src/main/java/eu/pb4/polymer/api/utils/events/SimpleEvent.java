package eu.pb4.polymer.api.utils.events;


import net.fabricmc.fabric.api.event.EventFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SimpleEvent<T> {
    private List<T> handlers = new ArrayList<>();

    public void register(T listener) {
        this.handlers.add(listener);
    }

    public void invoke(Consumer<T> invoker) {
        for (var handler : handlers) {
            invoker.accept(handler);
        }
    }
}
