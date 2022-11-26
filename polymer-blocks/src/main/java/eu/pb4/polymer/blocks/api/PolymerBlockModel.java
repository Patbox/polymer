package eu.pb4.polymer.blocks.api;

import net.minecraft.util.Identifier;

public record PolymerBlockModel(Identifier model, int x, int y, boolean uvLock, int weight) {
    public static PolymerBlockModel of(Identifier model) {
        return of(model, 0, 0);
    }

    public static PolymerBlockModel of(Identifier model, int x, int y) {
        return of(model, x, y, 1);
    }

    public static PolymerBlockModel of(Identifier model, int x, int y, int weight) {
        return of(model, x, y, false, weight);
    }

    public static PolymerBlockModel of(Identifier model, int x, int y, boolean uvLock, int weight) {
        return new PolymerBlockModel(model, x, y, uvLock, weight);
    }
}
