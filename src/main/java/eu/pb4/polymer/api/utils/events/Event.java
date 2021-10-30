package eu.pb4.polymer.api.utils.events;

import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public final class Event<T> {
    private List<EventHandler<T>> handlers = new ArrayList<>();

    public void register(EventHandler<T> event) {
        this.handlers.add(event);
    }

    public void invoke(T obj) {
        for (EventHandler<T> consumer : this.handlers) {
            consumer.call(obj);
        }
    }


    public interface EventHandler<T> {
        void call(T obj);
    }
}
