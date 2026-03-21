package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PolymerCreativeTabContentClearS2CPayload(Identifier groupId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerCreativeTabContentClearS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.SYNC_CREATIVE_TAB_CONTENTS_CLEAR);

    public static final StreamCodec<ContextByteBuf, PolymerCreativeTabContentClearS2CPayload> CODEC = StreamCodec.ofMember(PolymerCreativeTabContentClearS2CPayload::write, PolymerCreativeTabContentClearS2CPayload::read);
    public void write(FriendlyByteBuf buf) {
        buf.writeIdentifier(this.groupId);
    }

    public static PolymerCreativeTabContentClearS2CPayload read(FriendlyByteBuf buf) {
        return new PolymerCreativeTabContentClearS2CPayload(buf.readIdentifier());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
