package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public record PolymerSyncFinishedS2CPayload()  implements CustomPacketPayload  {
    public static final CustomPacketPayload.Type<PolymerSyncFinishedS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_FINISHED);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
