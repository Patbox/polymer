package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupContentClearS2CPayload(Identifier groupId) implements CustomPayload {
    public static final CustomPayload.Id<PolymerItemGroupContentClearS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.SYNC_ITEM_GROUP_CONTENTS_CLEAR);

    public static final PacketCodec<ContextByteBuf, PolymerItemGroupContentClearS2CPayload> CODEC = PacketCodec.of(PolymerItemGroupContentClearS2CPayload::write, PolymerItemGroupContentClearS2CPayload::read);
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);
    }

    public static PolymerItemGroupContentClearS2CPayload read(PacketByteBuf buf) {
        return new PolymerItemGroupContentClearS2CPayload(buf.readIdentifier());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
