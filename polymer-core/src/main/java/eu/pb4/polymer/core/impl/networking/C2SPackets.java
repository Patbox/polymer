package eu.pb4.polymer.core.impl.networking;

import eu.pb4.polymer.core.impl.networking.payloads.c2s.PolymerChangeTooltipC2SPayload;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static eu.pb4.polymer.core.impl.PolymerImplUtils.id;

public class C2SPackets {
    public static final Identifier CHANGE_TOOLTIP = id("other/change_tooltip");

    public static <T extends CustomPacketPayload> void register(Identifier id, StreamCodec<ContextByteBuf, T> codec, int... ver) {
        PolymerNetworking.registerC2SVersioned(id, IntList.of(ver), codec);
    }

    static {
        register(CHANGE_TOOLTIP, PolymerChangeTooltipC2SPayload.CODEC, 6);
    }
}
