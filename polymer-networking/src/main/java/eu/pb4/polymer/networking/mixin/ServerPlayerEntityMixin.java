package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.api.server.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TempPlayerLoginAttachments {
    @Unique
    private boolean polymerNet$requireWorldReload;

    @Unique
    private PolymerHandshakeHandler polymerNet$handshakeHandler;
    @Unique
    private List<CustomPayloadC2SPacket> polymerNet$latePackets;
    @Unique
    private boolean polymerNet$forceRespawnPacket;

    @Override
    public void polymerNet$setWorldReload(boolean value) {
        this.polymerNet$requireWorldReload = value;
    }

    @Override
    public boolean polymerNet$getWorldReload() {
        return this.polymerNet$requireWorldReload;
    }

    @Override
    public PolymerHandshakeHandler polymerNet$getAndRemoveHandshakeHandler() {
        var handler = this.polymerNet$handshakeHandler;
        this.polymerNet$handshakeHandler = null;
        return handler;
    }

    @Override
    public PolymerHandshakeHandler polymerNet$getHandshakeHandler() {
        return this.polymerNet$handshakeHandler;
    }

    @Override
    public void polymerNet$setLatePackets(List<CustomPayloadC2SPacket> packets) {
        this.polymerNet$latePackets = packets;
    }

    @Override
    public List<CustomPayloadC2SPacket> polymerNet$getLatePackets() {
        return this.polymerNet$latePackets;
    }

    @Override
    public void polymerNet$setHandshakeHandler(PolymerHandshakeHandler handler) {
        this.polymerNet$handshakeHandler = handler;
    }

    @Override
    public void polymerNet$setForceRespawnPacket() {
        this.polymerNet$forceRespawnPacket = true;
    }

    @Override
    public boolean polymerNet$getForceRespawnPacket() {
        return this.polymerNet$forceRespawnPacket;
    }
}
