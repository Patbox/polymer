package eu.pb4.polymer.core.impl.networking.payloads;

import eu.pb4.polymer.core.impl.PolymerImpl;
import eu.pb4.polymer.core.impl.ServerMetadataKeys;
import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.payload.VersionedPayload;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.NbtInt;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import xyz.nucleoid.packettweaker.PacketContext;

public interface PayloadUtil {
    int PROTOCOL = SharedConstants.getProtocolVersion();

    static boolean matchesProtocol(PacketContext context) {
        var data = PolymerNetworking.getMetadata(context.getClientConnection(), ServerMetadataKeys.MINECRAFT_PROTOCOL, NbtInt.TYPE);
        return (data == null || data.intValue() == PROTOCOL) && clientCheck();
    }

    static boolean clientCheck() {
        if (PolymerImpl.IS_CLIENT) {
            return InternalClientRegistry.enabled;
        }

        return true;
    }

    static <T extends VersionedPayload> VersionedPayload.Decoder<T> checked(VersionedPayload.Decoder<T> decoder) {
        return (PacketContext context, Identifier identifier, int version, PacketByteBuf buf) -> {
            if (matchesProtocol(context)) {
                try {
                    return decoder.readPacket(context, identifier, version, buf);
                } catch (Throwable ignored) {}
            }
            return null;
        };
    }
}
