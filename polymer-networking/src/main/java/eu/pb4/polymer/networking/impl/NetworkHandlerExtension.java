package eu.pb4.polymer.networking.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface NetworkHandlerExtension {
    boolean polymerNet$hasPolymer();
    String polymerNet$version();
    int polymerNet$protocolVersion();

    void polymerNet$setVersion(String version);

    long polymerNet$lastPacketUpdate(Identifier identifier);
    void polymerNet$savePacketTime(Identifier identifier);

    int polymerNet$getSupportedVersion(Identifier identifier);
    void polymerNet$setSupportedVersion(Identifier identifier, int i);
    Object2IntMap<Identifier> polymerNet$getSupportMap();

    @Deprecated(forRemoval = true)
    default Object2IntMap<Identifier> polymer$getSupportMap() {
        return polymerNet$getSupportMap();
    }

    static NetworkHandlerExtension of(ServerPlayerEntity player) {
        return (NetworkHandlerExtension) player.networkHandler;
    }

    static NetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (NetworkHandlerExtension) handler;
    }


    void polymerNet$resetSupported();
}