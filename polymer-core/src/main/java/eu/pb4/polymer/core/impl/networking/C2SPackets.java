package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickBlockC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerPickEntityC2SPayload;
import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Identifier;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

public class C2SPackets {
    public static final Identifier WORLD_PICK_BLOCK = id("world/pick_block");
    public static final Identifier WORLD_PICK_ENTITY = id("world/pick_entity");
    public static final Identifier CHANGE_TOOLTIP = id("other/change_tooltip");

    public static void register(Identifier id, VersionedPayload.Decoder<?> decoder, int... ver) {
        PolymerNetworking.registerC2SPayload(id, IntList.of(ver), decoder);
    }

    static {
        register(WORLD_PICK_BLOCK, PolymerPickBlockC2SPayload::read,5);
        register(WORLD_PICK_ENTITY, PolymerPickEntityC2SPayload::read,5);
        register(CHANGE_TOOLTIP, PolymerChangeTooltipC2SPayload::read,5);
    }
}
