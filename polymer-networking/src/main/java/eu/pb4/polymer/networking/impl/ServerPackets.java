package eu.pb4.polymer.networking.impl;


import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@ApiStatus.Internal
public class ServerPackets {
    public static final Map<Identifier, int[]> REGISTRY = new HashMap<>();
    public static final Identifier HANDSHAKE = new Identifier("polymer", "handshake");
    public static final Identifier DISABLE = new Identifier("polymer", "disable");
    public static final Identifier METADATA = new Identifier("polymer", "metadata");


    public static final int getBestSupported(Identifier identifier, int[] ver) {

        var values = REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter((i) -> verSet.contains(i)).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static final void register(Identifier id, int... ver) {
        REGISTRY.put(id, ver);
    }


    static {
        register(HANDSHAKE, 1);
        register(DISABLE, 1);
        register(METADATA, 1);
    }

    public static void register() {
    }
}
