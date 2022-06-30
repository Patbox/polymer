package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.networking.PolymerHandshakeHandler;
import eu.pb4.polymer.impl.interfaces.ScreenHandlerPlayerContext;
import eu.pb4.polymer.impl.interfaces.TempPlayerLoginAttachments;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements TempPlayerLoginAttachments {
    @Unique
    private boolean polymer_requireWorldReload;

    @Unique
    private PolymerHandshakeHandler polymer_handshakeHandler;

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
    public void polymer_setHandshakeHandler(PolymerHandshakeHandler handler) {
        this.polymer_handshakeHandler = handler;
    }

    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void polymer_setPlayerContext(ScreenHandler screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof ScreenHandlerPlayerContext context) {
            context.polymer_setPlayer((ServerPlayerEntity) (Object) this);
        }
    }
}
