package eu.pb4.polymer.networking.impl;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

@ApiStatus.Internal
public interface ExtClientConnection {
    static ExtClientConnection of(ServerCommonNetworkHandler networkHandler) {
        return of(NetworkHandlerExtension.of(networkHandler).polymerNet$getConnection());
    }

    boolean polymerNet$hasPolymer();
    String polymerNet$version();

    void polymerNet$setVersion(String version);

    int polymerNet$getSupportedVersion(Identifier identifier);
    void polymerNet$setSupportedVersion(Identifier identifier, int i);
    Object2IntMap<Identifier> polymerNet$getSupportMap();
    Object2ObjectMap<Identifier, NbtElement> polymerNet$getMetadataMap();

    void polymerNet$wrongPacketConsumer(Consumer<CustomPayloadC2SPacket> consumer);
    Channel polymerNet$getChannel();

    static ExtClientConnection of(ClientConnection connection) {
        return (ExtClientConnection) connection;
    }
}
