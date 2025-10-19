package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.packet.CustomPayload;

public record PolymerNoOpPayload() implements CustomPayload {
    public static final Id<PolymerNoOpPayload> ID = new Id<>(S2CPackets.SYNC_STARTED);
    public static final PolymerNoOpPayload INSTANCE = new PolymerNoOpPayload();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
