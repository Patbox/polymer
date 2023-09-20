package eu.pb4.polymer.networking.impl;


import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@ApiStatus.Internal
public class ServerPackets {
    public static final Map<Identifier, int[]> REGISTRY = new HashMap<>();
    public static final Object2IntOpenHashMap<Identifier> LATEST = new Object2IntOpenHashMap<>();

    public static int getBestSupported(Identifier identifier, int[] ver) {

        var values = REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter(verSet::contains).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static void register(Identifier id, int... ver) {
        REGISTRY.put(id, ver);
        LATEST.put(id, getBestSupported(id, ver));
    }
}
