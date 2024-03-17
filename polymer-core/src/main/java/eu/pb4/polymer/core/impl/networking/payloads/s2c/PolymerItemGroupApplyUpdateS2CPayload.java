package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupApplyUpdateS2CPayload() implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupApplyUpdateS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_APPLY_UPDATE);


    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
