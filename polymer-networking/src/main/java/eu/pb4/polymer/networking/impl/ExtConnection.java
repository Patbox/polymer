package eu.pb4.polymer.networking.impl;

import io.netty.channel.Channel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;

@ApiStatus.Internal
public interface ExtConnection {
    static ExtConnection of(ServerCommonPacketListenerImpl networkHandler) {
        return of(PacketListenerImplExtension.of(networkHandler).polymerNet$getConnection());
    }

    boolean polymerNet$hasPolymer();
    String polymerNet$version();

    void polymerNet$setVersion(String version);

    int polymerNet$getSupportedVersion(Identifier identifier);
    void polymerNet$setSupportedVersion(Identifier identifier, int i);
    Object2IntMap<Identifier> polymerNet$getSupportMap();
    Object2ObjectMap<Identifier, Tag> polymerNet$getMetadataMap();

    void polymerNet$wrongPacketConsumer(Consumer<Packet<?>> consumer);
    Channel polymerNet$getChannel();

    static ExtConnection of(Connection connection) {
        return (ExtConnection) connection;
    }
}
