package eu.pb4.polymer.api.networking;

import eu.pb4.polymer.impl.networking.PolymerHandshakeHandlerImplLate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public interface PolymerHandshakeHandler {
    void sendPacket(Packet<?> packet);
    void set(String polymerVersion, Object2IntMap<String> protocolVersions);

    boolean isPolymer();

    String getPolymerVersion();

    int getSupportedProtocol(String identifier);

    void setLastPacketTime(String identifier);

    long getLastPacketTime(String identifier);

    MinecraftServer getServer();

    boolean shouldUpdateWorld();

    @Nullable
    ServerPlayerEntity getPlayer();

    static PolymerHandshakeHandler of(ServerPlayNetworkHandler handler) {
        return PolymerHandshakeHandlerImplLate.of(handler);
    }

    void apply(ServerPlayNetworkHandler handler);
}