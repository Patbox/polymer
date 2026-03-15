package eu.pb4.polymer.common.impl;


import com.google.common.collect.MapMaker;
import eu.pb4.polymer.common.mixin.ArrayBackedEventAccessor;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

// Todo: Make it works
public final class ProxyEvent<T> {
    private static final Map<Event<Object>, ProxyEvent<Object>> PROXIES = new MapMaker().weakKeys().makeMap();

    private final Event<T> backing;
    private final List<T> callbacks = new ArrayList<>();

    public ProxyEvent(Event<T> backing) {
        this.backing = backing;
    }

    @SuppressWarnings("unchecked")
    public static <T> ProxyEvent<T> of(Event<T> event, Function<List<T>, T> function) {
        return (ProxyEvent<T>) PROXIES.computeIfAbsent((Event<Object>) event, ProxyEvent::new);
    }

    public void register(T event) {

    }

    public T registerRet(T event) {
        return event;
    }

    public void unregister(T event) {

    }
}
