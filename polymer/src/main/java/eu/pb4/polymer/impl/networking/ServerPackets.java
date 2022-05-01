package eu.pb4.polymer.impl.networking;


import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static eu.pb4.polymer.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class ServerPackets {
    public static final Map<String, int[]> REGISTRY = new HashMap<>();
    public static final String HANDSHAKE = "handshake";
    public static final Identifier HANDSHAKE_ID = id(HANDSHAKE);
    public static final String SYNC_STARTED = "sync/started";
    public static final Identifier SYNC_STARTED_ID = id(SYNC_STARTED);
    public static final String SYNC_INFO = "sync/info";
    public static final Identifier SYNC_INFO_ID = id(SYNC_INFO);
    public static final String SYNC_FINISHED = "sync/finished";
    public static final Identifier SYNC_FINISHED_ID = id(SYNC_FINISHED);
    public static final String SYNC_BLOCK = "sync/blocks";
    public static final Identifier SYNC_BLOCK_ID = id(SYNC_BLOCK);
    public static final String SYNC_BLOCK_ENTITY = "sync/block_entities";
    public static final Identifier SYNC_BLOCK_ENTITY_ID = id(SYNC_BLOCK_ENTITY);
    public static final String SYNC_ITEM = "sync/items";
    public static final Identifier SYNC_ITEM_ID = id(SYNC_ITEM);
    public static final String SYNC_ENTITY = "sync/entities";
    public static final Identifier SYNC_ENTITY_ID = id(SYNC_ENTITY);
    public static final String SYNC_STATUS_EFFECT = "sync/status_effect";
    public static final Identifier SYNC_STATUS_EFFECT_ID = id(SYNC_STATUS_EFFECT);
    public static final String SYNC_VILLAGER_PROFESSION = "sync/villager_profession";
    public static final Identifier SYNC_VILLAGER_PROFESSION_ID = id(SYNC_VILLAGER_PROFESSION);
    public static final String SYNC_ITEM_GROUP = "sync/item_groups";
    public static final Identifier SYNC_ITEM_GROUP_ID = id(SYNC_ITEM_GROUP);
    public static final String SYNC_ITEM_GROUP_REMOVE = "sync/item_group/remove";
    public static final Identifier SYNC_ITEM_GROUP_REMOVE_ID = id(SYNC_ITEM_GROUP_REMOVE);
    public static final String SYNC_ITEM_GROUP_VANILLA = "sync/item_group/vanilla";
    public static final Identifier SYNC_ITEM_GROUP_VANILLA_ID = id(SYNC_ITEM_GROUP_VANILLA);
    public static final String SYNC_ITEM_GROUP_CLEAR = "sync/item_group/clear";
    public static final Identifier SYNC_ITEM_GROUP_CLEAR_ID = id(SYNC_ITEM_GROUP_CLEAR);
    public static final String SYNC_BLOCKSTATE = "sync/blockstate";
    public static final Identifier SYNC_BLOCKSTATE_ID = id(SYNC_BLOCKSTATE);
    public static final String SYNC_REBUILD_SEARCH = "sync/rebuild_search";
    public static final Identifier SYNC_REBUILD_SEARCH_ID = id(SYNC_REBUILD_SEARCH);
    public static final String SYNC_CLEAR = "sync/clear_all";
    public static final Identifier SYNC_CLEAR_ID = id(SYNC_CLEAR);
    public static final String WORLD_SET_BLOCK_UPDATE = "world/set_block";
    public static final Identifier WORLD_SET_BLOCK_UPDATE_ID = id(WORLD_SET_BLOCK_UPDATE);
    public static final String WORLD_CHUNK_SECTION_UPDATE = "world/section";
    public static final Identifier WORLD_CHUNK_SECTION_UPDATE_ID = id(WORLD_CHUNK_SECTION_UPDATE);
    public static final String WORLD_ENTITY = "world/entity";
    public static final Identifier WORLD_ENTITY_ID = id(WORLD_ENTITY);


    public static final int getBestSupported(String identifier, int[] ver) {

        var values = REGISTRY.get(identifier);

        if (values != null) {
            var verSet = new IntArraySet(ver);

            var value = IntStream.of(values).filter((i) -> verSet.contains(i)).max();

            return value.isPresent() ? value.getAsInt() : -1;
        }

        return -1;
    }

    public static final void register(String id, int... ver) {
        REGISTRY.put(id, ver);
    }


    static {
        register(HANDSHAKE, 0);
        register(SYNC_STARTED, 0);
        register(SYNC_INFO, 0);
        register(SYNC_FINISHED, 0);
        register(SYNC_BLOCK, 0);
        register(SYNC_BLOCK_ENTITY, 0);
        register(SYNC_BLOCKSTATE, 0);
        register(SYNC_ITEM, 2);
        register(SYNC_VILLAGER_PROFESSION, 0);
        register(SYNC_ITEM_GROUP, 0);
        register(SYNC_ITEM_GROUP_CLEAR, 0);
        register(SYNC_ITEM_GROUP_REMOVE, 0);
        register(SYNC_ITEM_GROUP_VANILLA, 0);
        register(SYNC_ENTITY, 0, 1);
        register(SYNC_STATUS_EFFECT, 0);
        register(SYNC_CLEAR, 0);
        register(WORLD_SET_BLOCK_UPDATE, 1);
        register(WORLD_CHUNK_SECTION_UPDATE, 1);
        register(WORLD_ENTITY, 0);
    }
}
