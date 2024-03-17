package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerEntityS2CPayload(int entityId, Identifier typeId) implements CustomPayload {
    public static final CustomPayload.Id<PolymerEntityS2CPayload> ID = new CustomPayload.Id<>(S2CPackets.WORLD_ENTITY);
    public static final PacketCodec<ContextByteBuf, PolymerEntityS2CPayload> CODEC = PacketCodec.of(PolymerEntityS2CPayload::write, PolymerEntityS2CPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeIdentifier(this.typeId);
    }


    public static PolymerEntityS2CPayload read(PacketByteBuf buf) {
        return new PolymerEntityS2CPayload(buf.readVarInt(), buf.readIdentifier());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
