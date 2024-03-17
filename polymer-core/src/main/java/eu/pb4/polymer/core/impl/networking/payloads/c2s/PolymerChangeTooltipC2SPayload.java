package eu.pb4.polymer.core.impl.networking.payloads.c2s;

import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerChangeTooltipC2SPayload(boolean advanced) implements CustomPayload {
    public static final CustomPayload.Id<DisableS2CPayload> ID = new CustomPayload.Id<>(C2SPackets.CHANGE_TOOLTIP);

    public static final PacketCodec<ContextByteBuf, PolymerChangeTooltipC2SPayload> CODEC =
            PacketCodec.of(PolymerChangeTooltipC2SPayload::write, PolymerChangeTooltipC2SPayload::read);
    public void write(PacketByteBuf buf) {
        buf.writeBoolean(advanced);
    }

    public static PolymerChangeTooltipC2SPayload read(PacketByteBuf buf) {
        return new PolymerChangeTooltipC2SPayload(buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
