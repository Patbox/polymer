package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.utils.PolymerUtils;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class ClientPackets {
    public static final Map<String, int[]> REGISTRY = new HashMap<>();
    public static final String HANDSHAKE = "handshake";
    public static final Identifier HANDSHAKE_ID = PolymerUtils.id(HANDSHAKE);
    public static final String SYNC_REQUEST = "sync/request";
    public static final Identifier SYNC_REQUEST_ID = PolymerUtils.id(SYNC_REQUEST);
    public static final String WORLD_PICK_BLOCK = "world/pick_block";
    public static final Identifier WORLD_PICK_BLOCK_ID = PolymerUtils.id(WORLD_PICK_BLOCK);
    public static final String WORLD_PICK_ENTITY = "world/pick_entity";
    public static final Identifier WORLD_PICK_ENTITY_ID = PolymerUtils.id(WORLD_PICK_ENTITY);

    public static int getBestSupported(String identifier, int[] ver) {

        var values = REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter(verSet::contains).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static void register(String id, int... ver) {
        REGISTRY.put(id, ver);
    }

    static {
        register(HANDSHAKE, 0);
        register(WORLD_PICK_BLOCK, 0);
        register(WORLD_PICK_ENTITY, 0);
    }
}
