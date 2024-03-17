package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSyncClearS2CPayload()  implements CustomPayload {
    public static final CustomPayload.Id<PolymerSyncClearS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_CLEAR);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
