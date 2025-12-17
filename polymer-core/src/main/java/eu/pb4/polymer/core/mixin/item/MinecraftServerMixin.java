package eu.pb4.polymer.core.mixin.item;

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.impl.networking.entry.PolymerBlockStateEntry;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"))
    private void polymerCore$beforeSetup(CallbackInfo info) {
        PolymerItemGroupUtils.invalidateItemGroupCache();
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;buildServerStatus()Lnet/minecraft/network/protocol/status/ServerStatus;", ordinal = 0))
    private void polymerCore$beforeStartTicking(CallbackInfo info) {
        PolymerBlockStateEntry.CACHE.clear();
        PolymerItemGroupUtils.invalidateItemGroupCache();
    }

    @Inject(method = "stopServer", at = @At("TAIL"))
    private void polymerCore$shutdown(CallbackInfo info) {
        PolymerBlockStateEntry.CACHE.clear();
        PolymerItemGroupUtils.invalidateItemGroupCache();
    }
}
