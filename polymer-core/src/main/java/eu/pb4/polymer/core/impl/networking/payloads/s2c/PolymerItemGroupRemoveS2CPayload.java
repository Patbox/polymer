package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupRemoveS2CPayload(Identifier groupId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerItemGroupRemoveS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_ITEM_GROUP_REMOVE);
    public static final StreamCodec<ContextByteBuf, PolymerItemGroupRemoveS2CPayload> CODEC = Identifier.STREAM_CODEC.map(PolymerItemGroupRemoveS2CPayload::new, PolymerItemGroupRemoveS2CPayload::groupId).cast();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
