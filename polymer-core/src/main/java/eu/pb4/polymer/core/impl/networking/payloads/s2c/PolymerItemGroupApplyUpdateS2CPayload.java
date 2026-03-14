package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record PolymerItemGroupApplyUpdateS2CPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerItemGroupApplyUpdateS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_ITEM_GROUP_APPLY_UPDATE);


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
