package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PolymerCreativeTabApplyUpdateS2CPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerCreativeTabApplyUpdateS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_CREATIVE_TAB_APPLY_UPDATE);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
