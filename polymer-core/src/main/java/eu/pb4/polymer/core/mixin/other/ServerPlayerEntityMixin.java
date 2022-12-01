package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.ScreenHandlerPlayerContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onScreenHandlerOpened", at = @At("HEAD"))
    private void polymer$setPlayerContext(ScreenHandler screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof ScreenHandlerPlayerContext context) {
            context.polymer$setPlayer((ServerPlayerEntity) (Object) this);
        }
    }
}
