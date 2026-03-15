package eu.pb4.polymer.common.impl;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EventImplUtils {
    static Event<Runnable> createRunnableEvent() {
        return EventFactory.createArrayBacked(Runnable.class, arr -> () -> {
            for (var c : arr) c.run();
        });
    }

    static <X, Y> Event<BiConsumer<X, Y>> createBiConsumerEvent() {
        return EventFactory.createArrayBacked(BiConsumer.class, arr -> (x, y) -> {
            for (var c : arr) c.accept(x, y);
        });
    }

    static <X, Y> Event<BiPredicate<X, Y>> createBiPredicateEvent() {
        return EventFactory.createArrayBacked(BiPredicate.class, arr -> (x, y) -> {
            for (var c : arr) {
                if (c.test(x, y)) {
                    return true;
                }
            }
            return false;
        });
    }

    static <X> Event<Predicate<X>> createPredicateEvent() {
        return EventFactory.createArrayBacked(Predicate.class, arr -> (x) -> {
            for (var c : arr) {
                if (c.test(x)) {
                    return true;
                }
            }
            return false;
        });
    }

    static <X> Event<Consumer<X>> createConsumerEvent() {
        return EventFactory.createArrayBacked(Consumer.class, arr -> (x) -> {
            for (var c : arr) c.accept(x);
        });
    }

    static <T> void copyEvent(Event<T> from, Event<T> to) {
        try {
            // Todo
        } catch (Throwable e) {
            CommonImpl.LOGGER.error("Failed to copy an event!", e);
        }
    }

    static boolean isEmpty(Event<?> event) {
        // Todo
        return false;
    }
}
