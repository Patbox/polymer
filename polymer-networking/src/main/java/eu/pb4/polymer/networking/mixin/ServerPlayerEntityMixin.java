package eu.pb4.polymer.networking.mixin;

import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.impl.TempPlayerLoginAttachments;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TempPlayerLoginAttachments {
    @Unique
    private boolean polymer$requireWorldReload;

    @Unique
    private PolymerHandshakeHandler polymer$handshakeHandler;
    @Unique
    private List<CustomPayloadC2SPacket> polymer$latePackets;
    @Unique
    private boolean polymer$forceRespawnPacket;

    @Override
    public void polymer$setWorldReload(boolean value) {
        this.polymer$requireWorldReload = value;
    }

    @Override
    public boolean polymer$getWorldReload() {
        return this.polymer$requireWorldReload;
    }

    @Override
    public PolymerHandshakeHandler polymer$getAndRemoveHandshakeHandler() {
        var handler = this.polymer$handshakeHandler;
        this.polymer$handshakeHandler = null;
        return handler;
    }

    @Override
    public PolymerHandshakeHandler polymer$getHandshakeHandler() {
        return this.polymer$handshakeHandler;
    }

    @Override
    public void polymer$setLatePackets(List<CustomPayloadC2SPacket> packets) {
        this.polymer$latePackets = packets;
    }

    @Override
    public List<CustomPayloadC2SPacket> polymer$getLatePackets() {
        return this.polymer$latePackets;
    }

    @Override
    public void polymer$setHandshakeHandler(PolymerHandshakeHandler handler) {
        this.polymer$handshakeHandler = handler;
    }

    @Override
    public void polymer$setForceRespawnPacket() {
        this.polymer$forceRespawnPacket = true;
    }

    @Override
    public boolean polymer$getForceRespawnPacket() {
        return this.polymer$forceRespawnPacket;
    }
}
