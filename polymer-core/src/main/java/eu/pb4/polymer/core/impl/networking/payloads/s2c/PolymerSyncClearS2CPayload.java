package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerSyncClearS2CPayload() implements VersionedPayload {
    public static final Identifier ID = S2CPackets.SYNC_CLEAR;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerSyncClearS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerSyncClearS2CPayload();
    }
}
