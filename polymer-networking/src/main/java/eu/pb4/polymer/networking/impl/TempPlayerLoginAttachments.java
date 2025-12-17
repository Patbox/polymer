package eu.pb4.polymer.networking.impl;

import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

@ApiStatus.Internal
public interface TempPlayerLoginAttachments {
    void polymerNet$setWorldReload(boolean value);
    boolean polymerNet$getWorldReload();

    PolymerHandshakeHandler polymerNet$getAndRemoveHandshakeHandler();
    PolymerHandshakeHandler polymerNet$getHandshakeHandler();
    void polymerNet$setLatePackets(List<ServerboundCustomPayloadPacket> packets);
    List<ServerboundCustomPayloadPacket> polymerNet$getLatePackets();

    void polymerNet$setHandshakeHandler(PolymerHandshakeHandler handler);

    void polymerNet$setForceRespawnPacket();
    boolean polymerNet$getForceRespawnPacket();
}
