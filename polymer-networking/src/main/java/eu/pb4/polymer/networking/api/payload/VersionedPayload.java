package eu.pb4.polymer.networking.api.payload;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.networking.impl.ClientPackets;
import eu.pb4.polymer.networking.impl.ExtClientConnection;
import eu.pb4.polymer.networking.impl.ServerPackets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public interface VersionedPayload extends CustomPayload {
    void write(PacketContext context, int version, PacketByteBuf buf);

    @Override
    default void write(PacketByteBuf buf) {
        var context = PacketContext.get();
        int version = -1;
        if (context.getClientConnection() != null) {
            version = ExtClientConnection.of(context.getClientConnection()).polymerNet$getSupportedVersion(this.id());
        }

        if (version == -1) {
            version = PolymerCommonUtils.isServerBound() ? ServerPackets.LATEST.getInt(id()) : ClientPackets.LATEST.getInt(id());
        }

        buf.writeVarInt(version);
        write(context, version, buf);
    }

    interface Decoder<T extends VersionedPayload> extends PayloadDecoder<T> {
        @Nullable
        T readPacket(PacketContext context, Identifier identifier, int version, PacketByteBuf buf);

        @Override
        default @Nullable T readPacket(Identifier identifier, PacketByteBuf buf) {
            return readPacket(PacketContext.get(), identifier, buf.readVarInt(), buf);
        }
    }
}
