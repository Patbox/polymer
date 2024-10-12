package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Internal
public interface TempPlayerLoginAttachments {
    void polymerNet$setWorldReload(boolean value);
    boolean polymerNet$getWorldReload();

    PolymerHandshakeHandler polymerNet$getAndRemoveHandshakeHandler();
    PolymerHandshakeHandler polymerNet$getHandshakeHandler();
    void polymerNet$setLatePackets(List<CustomPayloadC2SPacket> packets);
    List<CustomPayloadC2SPacket> polymerNet$getLatePackets();

    void polymerNet$setHandshakeHandler(PolymerHandshakeHandler handler);

    void polymerNet$setForceRespawnPacket();
    boolean polymerNet$getForceRespawnPacket();
}
