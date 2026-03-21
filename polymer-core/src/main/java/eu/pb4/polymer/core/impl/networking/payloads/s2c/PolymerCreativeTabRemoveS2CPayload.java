package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PolymerCreativeTabRemoveS2CPayload(Identifier groupId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerCreativeTabRemoveS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_CREATIVE_TAB_REMOVE);
    public static final StreamCodec<ContextByteBuf, PolymerCreativeTabRemoveS2CPayload> CODEC = Identifier.STREAM_CODEC.map(PolymerCreativeTabRemoveS2CPayload::new, PolymerCreativeTabRemoveS2CPayload::groupId).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
