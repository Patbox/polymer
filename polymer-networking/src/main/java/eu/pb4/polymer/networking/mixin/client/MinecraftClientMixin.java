package eu.pb4.polymer.networking.mixin.client;

import eu.pb4.polymer.networking.impl.client.ClientPacketRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setWorld(Lnet/minecraft/client/world/ClientWorld;)V"))
    private void polymerNet$onDisconnect(CallbackInfo ci) {
        ClientPacketRegistry.clear(null);
    }
}