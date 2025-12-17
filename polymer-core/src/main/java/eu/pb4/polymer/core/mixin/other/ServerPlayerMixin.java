package eu.pb4.polymer.core.mixin.other;

import eu.pb4.polymer.core.impl.interfaces.GenericPlayerContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "initMenu", at = @At("HEAD"))
    private void polymer$setPlayerContext(AbstractContainerMenu screenHandler, CallbackInfo ci) {
        if (screenHandler instanceof GenericPlayerContext context) {
            context.polymer$setPlayer((ServerPlayer) (Object) this);
        }
    }
}
