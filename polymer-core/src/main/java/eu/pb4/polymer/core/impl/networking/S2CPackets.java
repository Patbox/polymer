package eu.pb4.polymer.core.impl.networking;


import eu.pb4.polymer.core.impl.networking.entry.*;
import eu.pb4.polymer.core.impl.networking.payloads.*;
import eu.pb4.polymer.core.impl.networking.payloads.s2c.*;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

@ApiStatus.Internal
public class S2CPackets {
    public static final Identifier SYNC_STARTED = id("sync/started");
    public static final Identifier SYNC_FINISHED= id("sync/finished");
    public static final Identifier SYNC_BLOCK= id("sync/blocks");
    public static final Identifier SYNC_BLOCK_ENTITY = id("sync/block_entities");
    public static final Identifier SYNC_ITEM = id("sync/items");
    public static final Identifier SYNC_FLUID = id("sync/fluid");
    public static final Identifier SYNC_ENTITY = id("sync/entities");
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

    public static <T extends CustomPayload> void register(Identifier id, PacketCodec<ContextByteBuf, T> codec, int... ver) {
        PolymerNetworking.registerS2CVersioned(id, IntList.of(ver), codec);
    }

    public static <T extends CustomPayload> void register(Identifier id, Supplier<T> t, int... ver) {
        PolymerNetworking.registerS2CVersioned(id, IntList.of(ver), PacketCodec.unit(t.get()));
    }

    public static <T> CustomPayload.Id<PolymerGenericListPayload<T>> registerList(Identifier id, PacketCodec<ContextByteBuf, T> entry, int... ver) {
        var ide = new CustomPayload.Id<PolymerGenericListPayload<T>>(id);
        PolymerNetworking.registerS2CVersioned(ide, IntList.of(ver), PolymerGenericListPayload.codec(ide, entry));
        return ide;
    }

    public static final CustomPayload.Id<PolymerGenericListPayload<PolymerBlockEntry>> SYNC_BLOCK_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<PolymerBlockStateEntry>> SYNC_BLOCKSTATE_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<PolymerItemEntry>> SYNC_ITEM_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<PolymerEntityEntry>> SYNC_ENTITY_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<PolymerTagEntry>> SYNC_TAGS_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<DebugBlockStateEntry>> DEBUG_VALIDATE_STATES_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<IdValueEntry>> SYNC_FLUID_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<IdValueEntry>> SYNC_VILLAGER_PROFESSION_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<IdValueEntry>> SYNC_BLOCK_ENTITY_ID;
    public static final CustomPayload.Id<PolymerGenericListPayload<IdValueEntry>> SYNC_STATUS_EFFECT_ID;

    static {
        register(SYNC_STARTED, PolymerSyncStartedS2CPayload::new, 6);
        register(SYNC_FINISHED, PolymerSyncFinishedS2CPayload::new, 6);
        register(SYNC_CLEAR, PolymerSyncClearS2CPayload::new, 6);

        SYNC_BLOCK_ID = registerList(SYNC_BLOCK, PolymerBlockEntry.CODEC,8);
        SYNC_BLOCKSTATE_ID = registerList(SYNC_BLOCKSTATE, PolymerBlockStateEntry.CODEC, 8);
        SYNC_ITEM_ID = registerList(SYNC_ITEM, PolymerItemEntry.CODEC, 8);
        SYNC_ENTITY_ID = registerList(SYNC_ENTITY, PolymerEntityEntry.CODEC,8);
        SYNC_TAGS_ID = registerList(SYNC_TAGS, PolymerTagEntry.CODEC, 8);
        DEBUG_VALIDATE_STATES_ID = registerList(DEBUG_VALIDATE_STATES, DebugBlockStateEntry.CODEC, 6);

        SYNC_FLUID_ID = registerList(SYNC_FLUID, IdValueEntry.CODEC, 8);
        SYNC_VILLAGER_PROFESSION_ID = registerList(SYNC_VILLAGER_PROFESSION, IdValueEntry.CODEC, 8);
        SYNC_BLOCK_ENTITY_ID = registerList(SYNC_BLOCK_ENTITY, IdValueEntry.CODEC, 8);
        SYNC_STATUS_EFFECT_ID = registerList(SYNC_STATUS_EFFECT, IdValueEntry.CODEC, 8);


        register(SYNC_ITEM_GROUP_DEFINE, PolymerItemGroupDefineS2CPayload.CODEC,8);
        register(SYNC_ITEM_GROUP_CONTENTS_CLEAR, PolymerItemGroupContentClearS2CPayload.CODEC, 8);
        register(SYNC_ITEM_GROUP_REMOVE, PolymerItemGroupRemoveS2CPayload.CODEC,8);
        register(SYNC_ITEM_GROUP_CONTENTS_ADD, PolymerItemGroupContentAddS2CPayload.CODEC,8);
        register(SYNC_ITEM_GROUP_APPLY_UPDATE, PolymerItemGroupApplyUpdateS2CPayload::new, 8);

        register(WORLD_SET_BLOCK_UPDATE, PolymerBlockUpdateS2CPayload.CODEC,8);
        register(WORLD_CHUNK_SECTION_UPDATE, PolymerSectionUpdateS2CPayload.CODEC, 8);
        register(WORLD_ENTITY, PolymerEntityS2CPayload.CODEC, 8);
    }
}
