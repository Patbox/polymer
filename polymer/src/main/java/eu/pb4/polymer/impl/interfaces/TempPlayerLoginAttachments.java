package eu.pb4.polymer.impl.interfaces;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;

import java.util.List;

public interface TempPlayerLoginAttachments {
    void polymer_setWorldReload(boolean value);
    boolean polymer_getWorldReload();

    PolymerHandshakeHandler polymer_getAndRemoveHandshakeHandler();
    PolymerHandshakeHandler polymer_getHandshakeHandler();
    void polymer_setLatePackets(List<CustomPayloadC2SPacket> packets);
    List<CustomPayloadC2SPacket> polymer_getLatePackets();

    void polymer_setHandshakeHandler(PolymerHandshakeHandler handler);

    void polymer_setForceRespawnPacket();
    boolean polymer_getForceRespawnPacket();
}
