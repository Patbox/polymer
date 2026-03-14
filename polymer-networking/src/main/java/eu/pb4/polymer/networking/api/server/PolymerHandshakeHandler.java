package eu.pb4.polymer.networking.api.server;

import eu.pb4.polymer.networking.impl.PolymerHandshakeHandlerImplLate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@ApiStatus.NonExtendable
public interface PolymerHandshakeHandler {
    void sendPacket(Packet<?> packet);
    void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions);
    void setMetadataValue(Identifier identifier, Tag value);

    boolean isPolymer();

    String getPolymerVersion();

    int getSupportedProtocol(Identifier identifier);

    void setLastPacketTime(Identifier identifier);

    long getLastPacketTime(Identifier identifier);

    MinecraftServer getServer();

    @Nullable
    ServerPlayer getPlayer();

    static PolymerHandshakeHandler of(MinecraftServer server, ServerCommonPacketListenerImpl handler) {
        return PolymerHandshakeHandlerImplLate.of(server, handler);
    }

    void apply(ServerGamePacketListenerImpl handler);

    boolean getPackStatus(UUID uuid);

    void reset();

    void setPackStatus(UUID uuid, boolean status);
}