package eu.pb4.polymer.core.impl.networking;


import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.payloads.*;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class S2CPackets {
    public static final Identifier SYNC_STARTED = id("sync/started");
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
    public static final Identifier SYNC_ITEM_GROUP_APPLY_UPDATE = id("sync/item_group/apply_update");
    public static final Identifier SYNC_CLEAR= id("sync/clear_all");
    public static final Identifier WORLD_SET_BLOCK_UPDATE= id("world/set_block");
    public static final Identifier WORLD_CHUNK_SECTION_UPDATE= id("world/section");
    public static final Identifier WORLD_ENTITY = id("world/entity");

    public static final Identifier DEBUG_VALIDATE_STATES = id("debug/validate_states");

    public static void register(Identifier id, VersionedPayload.Decoder<?> decoder, int... ver) {
        PolymerNetworking.registerS2CPayload(id, IntList.of(ver), PayloadUtil.checked(decoder));
    }

    static {
        register(SYNC_STARTED, PolymerSyncStartedS2CPayload::read,5);
        register(SYNC_FINISHED, PolymerSyncFinishedS2CPayload::read,5);
        register(SYNC_CLEAR, PolymerSyncClearS2CPayload::read,5);

        register(SYNC_BLOCK, PolymerGenericListPayload::read,5);
        PolymerGenericListPayload.ENTRIES.put(SYNC_BLOCK, PolymerBlockEntry::read);
        register(SYNC_BLOCKSTATE, PolymerGenericListPayload::read,5);
        PolymerGenericListPayload.ENTRIES.put(SYNC_BLOCKSTATE, PolymerBlockStateEntry::read);
        register(SYNC_ITEM, PolymerGenericListPayload::read,5);
        PolymerGenericListPayload.ENTRIES.put(SYNC_ITEM, PolymerItemEntry::read);
        register(SYNC_ENTITY, PolymerGenericListPayload::read,5);
        PolymerGenericListPayload.ENTRIES.put(SYNC_ENTITY, PolymerEntityEntry::read);
        register(SYNC_TAGS, PolymerGenericListPayload::read,5);
        PolymerGenericListPayload.ENTRIES.put(SYNC_TAGS, PolymerTagEntry::read);
        register(DEBUG_VALIDATE_STATES, PolymerGenericListPayload::read, 5);
        PolymerGenericListPayload.ENTRIES.put(DEBUG_VALIDATE_STATES, DebugBlockStateEntry::read);


        register(SYNC_FLUID, PolymerGenericListPayload::read,5);
        register(SYNC_ENCHANTMENT, PolymerGenericListPayload::read,5);
        register(SYNC_VILLAGER_PROFESSION, PolymerGenericListPayload::read,5);
        register(SYNC_BLOCK_ENTITY, PolymerGenericListPayload::read,5);
        register(SYNC_STATUS_EFFECT, PolymerGenericListPayload::read,5);


        register(SYNC_ITEM_GROUP_DEFINE, PolymerItemGroupDefineS2CPayload::read,5);
        register(SYNC_ITEM_GROUP_CONTENTS_CLEAR, PolymerItemGroupContentClearS2CPayload::read,5);
        register(SYNC_ITEM_GROUP_REMOVE, PolymerItemGroupRemoveS2CPayload::read,5);
        register(SYNC_ITEM_GROUP_CONTENTS_ADD, PolymerItemGroupContentAddS2CPayload::read,5);
        register(SYNC_ITEM_GROUP_APPLY_UPDATE, PolymerItemGroupApplyUpdateS2CPayload::read,5);

        register(WORLD_SET_BLOCK_UPDATE, PolymerBlockUpdateS2CPayload::read,5);
        register(WORLD_CHUNK_SECTION_UPDATE, PolymerSectionUpdateS2CPayload::read, 5);
        register(WORLD_ENTITY, PolymerEntityS2CPayload::read, 5);
    }
}
