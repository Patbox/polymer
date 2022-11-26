package eu.pb4.polymer.core.impl.networking;


import eu.pb4.polymer.core.impl.PolymerImplUtils;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@ApiStatus.Internal
public class ServerPackets {
    public static final Map<String, int[]> REGISTRY = new HashMap<>();
    public static final String HANDSHAKE = "handshake";
    public static final Identifier HANDSHAKE_ID = PolymerImplUtils.id(HANDSHAKE);
    public static final String DISABLE = "disable";
    public static final Identifier DISABLE_ID = PolymerImplUtils.id(DISABLE);
    public static final String SYNC_STARTED = "sync/started";
    public static final Identifier SYNC_STARTED_ID = PolymerImplUtils.id(SYNC_STARTED);
    public static final String SYNC_INFO = "sync/info";
    public static final Identifier SYNC_INFO_ID = PolymerImplUtils.id(SYNC_INFO);
    public static final String SYNC_FINISHED = "sync/finished";
    public static final Identifier SYNC_FINISHED_ID = PolymerImplUtils.id(SYNC_FINISHED);
    public static final String SYNC_BLOCK = "sync/blocks";
    public static final Identifier SYNC_BLOCK_ID = PolymerImplUtils.id(SYNC_BLOCK);
    public static final String SYNC_BLOCK_ENTITY = "sync/block_entities";
    public static final Identifier SYNC_BLOCK_ENTITY_ID = PolymerImplUtils.id(SYNC_BLOCK_ENTITY);
    public static final String SYNC_ITEM = "sync/items";
    public static final Identifier SYNC_ITEM_ID = PolymerImplUtils.id(SYNC_ITEM);
    public static final String SYNC_ENCHANTMENT = "sync/enchantments";
    public static final Identifier SYNC_ENCHANTMENT_ID = PolymerImplUtils.id(SYNC_ENCHANTMENT);
    public static final String SYNC_ENTITY = "sync/entities";
    public static final Identifier SYNC_ENTITY_ID = PolymerImplUtils.id(SYNC_ENTITY);
    public static final String SYNC_STATUS_EFFECT = "sync/status_effect";
    public static final Identifier SYNC_STATUS_EFFECT_ID = PolymerImplUtils.id(SYNC_STATUS_EFFECT);
    public static final String SYNC_VILLAGER_PROFESSION = "sync/villager_profession";
    public static final Identifier SYNC_VILLAGER_PROFESSION_ID = PolymerImplUtils.id(SYNC_VILLAGER_PROFESSION);
    public static final String SYNC_ITEM_GROUP_DEFINE = "sync/item_group/define";
    public static final Identifier SYNC_ITEM_GROUP_DEFINE_ID = PolymerImplUtils.id(SYNC_ITEM_GROUP_DEFINE);
    public static final String SYNC_ITEM_GROUP_REMOVE = "sync/item_group/remove";
    public static final Identifier SYNC_ITEM_GROUP_REMOVE_ID = PolymerImplUtils.id(SYNC_ITEM_GROUP_REMOVE);
    public static final String SYNC_ITEM_GROUP_CONTENTS_ADD = "sync/item_group/contents/add";
    public static final Identifier SYNC_ITEM_GROUP_CONTENTS_ADD_ID = PolymerImplUtils.id(SYNC_ITEM_GROUP_CONTENTS_ADD);
    public static final String SYNC_ITEM_GROUP_CONTENTS_CLEAR = "sync/item_group/contents/clear";
    public static final Identifier SYNC_ITEM_GROUP_CONTENTS_CLEAR_ID = PolymerImplUtils.id(SYNC_ITEM_GROUP_CONTENTS_CLEAR);
    public static final String SYNC_BLOCKSTATE = "sync/blockstate";
    public static final Identifier SYNC_BLOCKSTATE_ID = PolymerImplUtils.id(SYNC_BLOCKSTATE);
    public static final String SYNC_TAGS = "sync/tags";
    public static final Identifier SYNC_TAGS_ID = PolymerImplUtils.id(SYNC_TAGS);
    public static final String SYNC_ITEM_GROUP_APPLY_UPDATE = "sync/item_group/apply_update";
    public static final Identifier SYNC_ITEM_GROUP_APPLY_UPDATE_ID = PolymerImplUtils.id(SYNC_ITEM_GROUP_APPLY_UPDATE);
    public static final String SYNC_CLEAR = "sync/clear_all";
    public static final Identifier SYNC_CLEAR_ID = PolymerImplUtils.id(SYNC_CLEAR);
    public static final String WORLD_SET_BLOCK_UPDATE = "world/set_block";
    public static final Identifier WORLD_SET_BLOCK_UPDATE_ID = PolymerImplUtils.id(WORLD_SET_BLOCK_UPDATE);
    public static final String WORLD_CHUNK_SECTION_UPDATE = "world/section";
    public static final Identifier WORLD_CHUNK_SECTION_UPDATE_ID = PolymerImplUtils.id(WORLD_CHUNK_SECTION_UPDATE);
    public static final String WORLD_ENTITY = "world/entity";
    public static final Identifier WORLD_ENTITY_ID = PolymerImplUtils.id(WORLD_ENTITY);

    public static final String DEBUG_VALIDATE_STATES = "debug/validate_states";
    public static final Identifier DEBUG_VALIDATE_STATES_ID = PolymerImplUtils.id(DEBUG_VALIDATE_STATES);


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
        register(DISABLE, 0);
        register(SYNC_STARTED, 0);
        register(SYNC_INFO, 0);
        register(SYNC_FINISHED, 0);
        register(SYNC_BLOCK, 0);
        register(SYNC_BLOCK_ENTITY, 0);
        register(SYNC_BLOCKSTATE, 1);
        register(SYNC_ITEM, 4);
        register(SYNC_ENCHANTMENT, 0);
        register(SYNC_VILLAGER_PROFESSION, 0);
        register(SYNC_ITEM_GROUP_DEFINE, 1);
        register(SYNC_ITEM_GROUP_CONTENTS_CLEAR, 1);
        register(SYNC_ITEM_GROUP_REMOVE, 1);
        register(SYNC_ITEM_GROUP_CONTENTS_ADD, 1);
        register(SYNC_ITEM_GROUP_APPLY_UPDATE, 1);
        register(SYNC_ENTITY, 1);
        register(SYNC_STATUS_EFFECT, 0);
        register(SYNC_TAGS, 0);
        register(SYNC_CLEAR, 0);
        register(WORLD_SET_BLOCK_UPDATE, 2);
        register(WORLD_CHUNK_SECTION_UPDATE, 2);
        register(WORLD_ENTITY, 0);
        register(DEBUG_VALIDATE_STATES, 0);
    }
}
