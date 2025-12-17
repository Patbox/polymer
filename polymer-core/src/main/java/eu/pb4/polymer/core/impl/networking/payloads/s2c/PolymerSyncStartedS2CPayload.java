package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSyncStartedS2CPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerSyncStartedS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_STARTED);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
