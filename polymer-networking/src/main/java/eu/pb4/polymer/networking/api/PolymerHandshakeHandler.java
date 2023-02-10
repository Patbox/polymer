package eu.pb4.polymer.networking.api;

import eu.pb4.polymer.networking.impl.PolymerHandshakeHandlerImplLate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface PolymerHandshakeHandler {
    void sendPacket(Packet<?> packet);
    void set(String polymerVersion, Object2IntMap<Identifier> protocolVersions);

    boolean isPolymer();

    String getPolymerVersion();

    int getSupportedProtocol(Identifier identifier);

    void setLastPacketTime(Identifier identifier);

    long getLastPacketTime(Identifier identifier);

    MinecraftServer getServer();

    @Nullable
    ServerPlayerEntity getPlayer();

    static PolymerHandshakeHandler of(ServerPlayNetworkHandler handler) {
        return PolymerHandshakeHandlerImplLate.of(handler);
    }

    void apply(ServerPlayNetworkHandler handler);

    boolean getPackStatus();

    void reset();

    void setPackStatus(boolean status);
}