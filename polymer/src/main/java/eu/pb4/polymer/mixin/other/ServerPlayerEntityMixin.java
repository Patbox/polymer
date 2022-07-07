package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.impl.interfaces.ScreenHandlerPlayerContext;
import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TempPlayerLoginAttachments {
    @Unique
    private boolean polymer_requireWorldReload;

    @Unique
    private PolymerHandshakeHandler polymer_handshakeHandler;
    @Unique
    private List<CustomPayloadC2SPacket> polymer_latePackets;
    @Unique
    private boolean polymer_forceRespawnPacket;

    @Override
    public void polymer_setWorldReload(boolean value) {
        this.polymer_requireWorldReload = value;
    }

    @Override
    public boolean polymer_getWorldReload() {
        return this.polymer_requireWorldReload;
    }

    @Override
    public PolymerHandshakeHandler polymer_getAndRemoveHandshakeHandler() {
        var handler = this.polymer_handshakeHandler;
        this.polymer_handshakeHandler = null;
        return handler;
    }

    @Override
    public PolymerHandshakeHandler polymer_getHandshakeHandler() {
        return this.polymer_handshakeHandler;
    }

    @Override
    public void polymer_setLatePackets(List<CustomPayloadC2SPacket> packets) {
        this.polymer_latePackets = packets;
    }

    @Override
    public List<CustomPayloadC2SPacket> polymer_getLatePackets() {
        return this.polymer_latePackets;
    }

    @Override
    public void polymer_setHandshakeHandler(PolymerHandshakeHandler handler) {
        this.polymer_handshakeHandler = handler;
    }

    @Override
    public void polymer_setForceRespawnPacket() {
        this.polymer_forceRespawnPacket = true;
    }

    @Override
    public boolean polymer_getForceRespawnPacket() {
        return this.polymer_forceRespawnPacket;
    }

    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void polymer_setPlayerContext(ScreenHandler screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof ScreenHandlerPlayerContext context) {
            context.polymer_setPlayer((ServerPlayerEntity) (Object) this);
        }
    }
}
