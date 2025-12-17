package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PolymerNoOpPayload() implements CustomPacketPayload {
    public static final Type<PolymerNoOpPayload> ID = new Type<>(S2CPackets.SYNC_STARTED);
    public static final PolymerNoOpPayload INSTANCE = new PolymerNoOpPayload();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
