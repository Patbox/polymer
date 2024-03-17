package eu.pb4.polymer.core.impl.networking.payloads.c2s;

import eu.pb4.polymer.core.impl.networking.C2SPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerPickEntityC2SPayload(int entityId, boolean control) implements CustomPayload {
    public static final CustomPayload.Id<DisableS2CPayload> ID = new CustomPayload.Id<>(C2SPackets.WORLD_PICK_ENTITY);
    public static final PacketCodec<ContextByteBuf, PolymerPickEntityC2SPayload> CODEC = PacketCodec.of(PolymerPickEntityC2SPayload::write, PolymerPickEntityC2SPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeBoolean(control);
    }

    public static PolymerPickEntityC2SPayload read(PacketByteBuf buf) {
        return new PolymerPickEntityC2SPayload(buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
