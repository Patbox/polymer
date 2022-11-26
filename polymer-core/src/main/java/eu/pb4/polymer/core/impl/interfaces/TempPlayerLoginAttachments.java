package eu.pb4.polymer.core.impl.interfaces;

import eu.pb4.polymer.core.api.networking.PolymerHandshakeHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

import java.util.List;

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
