package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupContentClearS2CPayload(Identifier groupId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerItemGroupContentClearS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR);

    public static final StreamCodec<ContextByteBuf, PolymerItemGroupContentClearS2CPayload> CODEC = StreamCodec.ofMember(PolymerItemGroupContentClearS2CPayload::write, PolymerItemGroupContentClearS2CPayload::read);
    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(this.groupId);
    }

    public static PolymerItemGroupContentClearS2CPayload read(FriendlyByteBuf buf) {
        return new PolymerItemGroupContentClearS2CPayload(buf.readIdentifier());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
