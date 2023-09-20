package eu.pb4.polymer.networking.impl.packets;

import eu.pb4.polymer.common.impl.CommonImpl;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import eu.pb4.polymer.networking.impl.ClientPackets;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public record HandshakePayload(String version, Map<Identifier, int[]> packetVersions) implements VersionedPayload {
    public static final Identifier ID = new Identifier("polymer", "handshake");

    @Override
    public void write(PacketContext context, int version, PacketByteBuf buf) {
        buf.writeString(this.version);
        buf.writeMap(packetVersions, PacketByteBuf::writeIdentifier, PacketByteBuf::writeIntArray);
    }

    @Override
    public Identifier id() {
        return ID;
    }

    public static HandshakePayload read(PacketContext context, Identifier identifier, int version, PacketByteBuf buf) {
        return new HandshakePayload(buf.readString(), buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readIntArray));
    }
}
