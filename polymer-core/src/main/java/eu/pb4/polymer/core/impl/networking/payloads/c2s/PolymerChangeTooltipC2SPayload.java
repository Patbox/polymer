package eu.pb4.polymer.core.impl.networking.payloads.c2s;

import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerChangeTooltipC2SPayload(boolean advanced) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DisableS2CPayload> ID = new CustomPacketPayload.Type<>(C2SPackets.CHANGE_TOOLTIP);

    public static final StreamCodec<ContextByteBuf, PolymerChangeTooltipC2SPayload> CODEC =
            StreamCodec.ofMember(PolymerChangeTooltipC2SPayload::write, PolymerChangeTooltipC2SPayload::read);
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(advanced);
    }

    public static PolymerChangeTooltipC2SPayload read(FriendlyByteBuf buf) {
        return new PolymerChangeTooltipC2SPayload(buf.readBoolean());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
