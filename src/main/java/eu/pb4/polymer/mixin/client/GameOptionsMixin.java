package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.networking.ClientPacketBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Shadow protected MinecraftClient client;

    @Inject(method = "sendClientSettings", at = @At("TAIL"))
    private void polymer_requestSync(CallbackInfo ci) {
        if (this.client.player != null) {
            ClientPacketBuilder.sendVersion(this.client.player.networkHandler);
        }
    }
}
