package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerEntityS2CPayload(int entityId, Identifier typeId) implements VersionedPayload {
    public static final Identifier ID = S2CPackets.WORLD_ENTITY;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeVarInt(this.entityId);
        buf.writeIdentifier(this.typeId);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerEntityS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerEntityS2CPayload(buf.readVarInt(), buf.readIdentifier());
    }
}
