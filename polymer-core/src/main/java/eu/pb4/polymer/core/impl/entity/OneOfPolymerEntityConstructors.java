package eu.pb4.polymer.core.impl.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;

import java.util.function.Function;

public record OneOfPolymerEntityConstructors<T>(Function<T, PolymerEntity> first, Function<T, PolymerEntity> second) implements Function<T, PolymerEntity> {
    @Override
    public PolymerEntity apply(T t) {
        var x = first.apply(t);
        if (x != null) return x;
        return second.apply(t);
    }
}
