package eu.pb4.polymer.networking.impl;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({"unused"})
public interface NetworkHandlerExtension {
    boolean polymer$hasPolymer();
    String polymer$version();
    int polymer$protocolVersion();

    void polymer$setVersion(String version);

    long polymer$lastPacketUpdate(Identifier identifier);
    void polymer$savePacketTime(Identifier identifier);

    int polymer$getSupportedVersion(Identifier identifier);
    void polymer$setSupportedVersion(Identifier identifier, int i);
    Object2IntMap<Identifier> polymer$getSupportMap();

    static NetworkHandlerExtension of(ServerPlayerEntity player) {
        return (NetworkHandlerExtension) player.networkHandler;
    }

    static NetworkHandlerExtension of(ServerPlayNetworkHandler handler) {
        return (NetworkHandlerExtension) handler;
    }


    void polymer$resetSupported();
}