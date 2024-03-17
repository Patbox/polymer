package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupRemoveS2CPayload(Identifier groupId) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupRemoveS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_REMOVE);
    public static final PacketCodec<ContextByteBuf, PolymerItemGroupRemoveS2CPayload> CODEC = Identifier.PACKET_CODEC.xmap(PolymerItemGroupRemoveS2CPayload::new, PolymerItemGroupRemoveS2CPayload::groupId).cast();

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
