package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.ContextByteBuf;
import eu.pb4.polymer.networking.impl.packets.DisableS2CPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerEntityS2CPayload(int entityId, Identifier typeId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PolymerEntityS2CPayload> ID = new CustomPacketPayload.Type<>(S2CPackets.WORLD_ENTITY);
    public static final StreamCodec<ContextByteBuf, PolymerEntityS2CPayload> CODEC = StreamCodec.ofMember(PolymerEntityS2CPayload::write, PolymerEntityS2CPayload::read);

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeIdentifier(this.typeId);
    }


    public static PolymerEntityS2CPayload read(FriendlyByteBuf buf) {
        return new PolymerEntityS2CPayload(buf.readVarInt(), buf.readIdentifier());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
