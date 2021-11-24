package eu.pb4.polymer.other;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class ContextAwareModifyEvent<T> {
    private final List<EventHandler<T>> handlers = new ArrayList<>();

    public void register(EventHandler<T> event) {
        this.handlers.add(event);
    }

    public T invoke(T og, T virtual, ServerPlayerEntity obj3) {
        for (EventHandler<T> consumer : this.handlers) {
            virtual = consumer.call(og, virtual, obj3);
        }
        return virtual;
    }


    public interface EventHandler<T> {
        T call(T original, T virtual, ServerPlayerEntity player);
    }
}
