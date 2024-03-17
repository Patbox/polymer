package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSyncFinishedS2CPayload()  implements CustomPayload  {
    public static final CustomPayload.Id<PolymerSyncFinishedS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_FINISHED);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
