package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public interface TempPlayerLoginAttachments {
    void polymer$setWorldReload(boolean value);
    boolean polymer$getWorldReload();

    PolymerHandshakeHandler polymer$getAndRemoveHandshakeHandler();
    PolymerHandshakeHandler polymer$getHandshakeHandler();
    void polymer$setLatePackets(List<CustomPayloadC2SPacket> packets);
    List<CustomPayloadC2SPacket> polymer$getLatePackets();

    void polymer$setHandshakeHandler(PolymerHandshakeHandler handler);

    void polymer$setForceRespawnPacket();
    boolean polymer$getForceRespawnPacket();
}
