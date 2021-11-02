package eu.pb4.polymer.impl.networking;

import eu.pb4.polymer.api.utils.PolymerUtils;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class PolymerPacketIds {
    public static final String VERSION = "version";
    public static final Identifier VERSION_ID = PolymerUtils.id(VERSION);

    public static final String REGISTRY_BLOCK = "registry/block";
    public static final Identifier REGISTRY_BLOCK_ID = PolymerUtils.id(REGISTRY_BLOCK);
    public static final String REGISTRY_ITEM = "registry/item";
    public static final Identifier REGISTRY_ITEM_ID = PolymerUtils.id(REGISTRY_ITEM);

    public static final String REGISTRY_ITEM_GROUP = "registry/item_group";
    public static final Identifier REGISTRY_ITEM_GROUP_ID = PolymerUtils.id(REGISTRY_ITEM_GROUP);

    public static final String REGISTRY_ITEM_GROUP_REMOVE = "registry/item_group/remove";
    public static final Identifier REGISTRY_ITEM_GROUP_REMOVE_ID = PolymerUtils.id(REGISTRY_ITEM_GROUP_REMOVE);


    public static final String REGISTRY_ITEM_GROUP_VANILLA = "registry/item_group/vanilla";
    public static final Identifier REGISTRY_ITEM_GROUP_VANILLA_ID = PolymerUtils.id(REGISTRY_ITEM_GROUP_VANILLA);

    public static final String REGISTRY_ITEM_GROUP_CLEAR = "registry/item_group/clear";
    public static final Identifier REGISTRY_ITEM_GROUP_CLEAR_ID = PolymerUtils.id(REGISTRY_ITEM_GROUP_CLEAR);

    public static final String REGISTRY_RESET_SEARCH = "registry/reset_search";
    public static final Identifier REGISTRY_RESET_SEARCH_ID = PolymerUtils.id(REGISTRY_RESET_SEARCH);

    public static final String REGISTRY_BLOCKSTATE = "registry/blockstate";
    public static final Identifier REGISTRY_BLOCKSTATE_ID = PolymerUtils.id(REGISTRY_BLOCKSTATE);

    public static final String REGISTRY_CLEAR = "registry/clear";
    public static final Identifier REGISTRY_CLEAR_ID = PolymerUtils.id(REGISTRY_CLEAR);

    public static final String BLOCK_UPDATE = "world/block";
    public static final Identifier BLOCK_UPDATE_ID = PolymerUtils.id(BLOCK_UPDATE);
    public static final String CHUNK_SECTION_UPDATE = "world/section";
    public static final Identifier CHUNK_SECTION_UPDATE_ID = PolymerUtils.id(CHUNK_SECTION_UPDATE);

    public static final String PICK_BLOCK = "pick_block";
    public static final Identifier PICK_BLOCK_ID = PolymerUtils.id(PICK_BLOCK);
}
