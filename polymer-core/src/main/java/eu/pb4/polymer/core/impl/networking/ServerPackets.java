package eu.pb4.polymer.core.impl.networking;


import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class ServerPackets {
    public static final Identifier SYNC_STARTED = id("sync/started");
    public static final Identifier SYNC_INFO = id("sync/info");
    public static final Identifier SYNC_FINISHED= id("sync/finished");
    public static final Identifier SYNC_BLOCK= id("sync/blocks");
    public static final Identifier SYNC_BLOCK_ENTITY = id("sync/block_entities");
    public static final Identifier SYNC_ITEM = id("sync/items");
    public static final Identifier SYNC_FLUID = id("sync/fluid");
    public static final Identifier SYNC_ENCHANTMENT= id("sync/enchantments");
    public static final Identifier SYNC_ENTITY= id("sync/entities");
    public static final Identifier SYNC_STATUS_EFFECT= id("sync/status_effect");
    public static final Identifier SYNC_VILLAGER_PROFESSION= id("sync/villager_profession");
    public static final Identifier SYNC_ITEM_GROUP_DEFINE= id("sync/item_group/define");
    public static final Identifier SYNC_ITEM_GROUP_REMOVE= id("sync/item_group/remove");
    public static final Identifier SYNC_ITEM_GROUP_CONTENTS_ADD= id("sync/item_group/contents/add");
    public static final Identifier SYNC_ITEM_GROUP_CONTENTS_CLEAR= id("sync/item_group/contents/clear");
    public static final Identifier SYNC_BLOCKSTATE= id("sync/blockstate");
    public static final Identifier SYNC_TAGS= id("sync/tags");
    public static final Identifier SYNC_ITEM_GROUP_APPLY_UPDATE= id("sync/item_group/apply_update");
    public static final Identifier SYNC_CLEAR= id("sync/clear_all");
    public static final Identifier WORLD_SET_BLOCK_UPDATE= id("world/set_block");
    public static final Identifier WORLD_CHUNK_SECTION_UPDATE= id("world/section");
    public static final Identifier WORLD_ENTITY = id("world/entity");

    public static final Identifier DEBUG_VALIDATE_STATES = id("debug/validate_states");

    public static final void register(Identifier id, int... ver) {
        PolymerServerNetworking.registerSendPacket(id, ver);
    }

    static {
        register(SYNC_STARTED, 0);
        register(SYNC_INFO, 0);
        register(SYNC_FINISHED, 0);
        register(SYNC_BLOCK, 0);
        register(SYNC_BLOCK_ENTITY, 0);
        register(SYNC_BLOCKSTATE, 1);
        register(SYNC_ITEM, 4);
        register(SYNC_FLUID, 1);
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
