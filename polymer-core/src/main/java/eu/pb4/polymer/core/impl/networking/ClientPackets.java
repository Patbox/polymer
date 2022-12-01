package eu.pb4.polymer.core.impl.networking;

import net.minecraft.util.Identifier;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

public class ClientPackets {
    public static final Identifier DISABLE = id("disable");
    public static final Identifier SYNC_REQUEST = id("sync/request");
    public static final Identifier WORLD_PICK_BLOCK = id("world/pick_block");
    public static final Identifier WORLD_PICK_ENTITY = id("world/pick_entity");
    public static final Identifier CHANGE_TOOLTIP = id("other/change_tooltip");
}
