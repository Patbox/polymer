package eu.pb4.polymer.core.impl.networking.payloads.s2c;

import eu.pb4.polymer.core.impl.networking.S2CPackets;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public record PolymerItemGroupRemoveS2CPayload(Identifier groupId) implements VersionedPayload {
    public static final Identifier ID = S2CPackets.SYNC_ITEM_GROUP_REMOVE;

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeIdentifier(this.groupId);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static PolymerItemGroupRemoveS2CPayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new PolymerItemGroupRemoveS2CPayload(buf.readIdentifier());
    }
}
