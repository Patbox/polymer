package eu.pb4.polymer.impl.interfaces;

import java.util.function.Consumer;

public interface MetaConsumer<T, E> extends Consumer<T> {
    E getAttached();
}
