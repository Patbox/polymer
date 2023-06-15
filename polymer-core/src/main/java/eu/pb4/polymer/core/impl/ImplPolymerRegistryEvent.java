package eu.pb4.polymer.core.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ApiStatus.Internal
public class ImplPolymerRegistryEvent {
    private static final Map<Registry<?>, List<Consumer<?>>> EVENTS = new Object2ObjectOpenCustomHashMap<>(Util.identityHashStrategy());
    public static void invokeRegistered(Registry<?> ts, Object entry) {
        //noinspection unchecked
        var x = (List<Consumer<Object>>) (Object) EVENTS.get(ts);

        if (x != null) {
            for (var a : x) {
                a.accept(entry);
            }
        }
    }

    public static <T> void register(Registry<T> registry, Consumer<T> tConsumer) {
        EVENTS.computeIfAbsent(registry, (a) -> new ArrayList<>()).add(tConsumer);
    }

    public static <T> void iterateAndRegister(Registry<T> registry, Consumer<T> tConsumer) {
        for (var x : registry) {
            tConsumer.accept(x);
        }
        register(registry, tConsumer);
    }
}
