package eu.pb4.polymer.other;

import java.util.ArrayList;
import java.util.List;

public final class BooleanEvent<T> {
    private List<EventHandler<T>> handlers = new ArrayList<>();

    public void register(EventHandler<T> event) {
        this.handlers.add(event);
    }

    public boolean invoke(T obj) {
        for (EventHandler<T> consumer : this.handlers) {
            boolean x = consumer.call(obj);
            if (x) {
                return true;
            }
        }
        return false;
    }


    public interface EventHandler<T> {
        boolean call(T obj);
    }
}
