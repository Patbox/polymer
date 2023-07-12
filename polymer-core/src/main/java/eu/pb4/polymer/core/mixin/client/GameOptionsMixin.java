package eu.pb4.polymer.core.mixin.client;

import eu.pb4.polymer.core.impl.client.InternalClientRegistry;
import eu.pb4.polymer.core.impl.client.networking.PolymerClientProtocol;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameOptions.class)
public class GameOptionsMixin {
    @Shadow protected MinecraftClient client;

    @Shadow public String language;
    @Unique String polymer$oldLang = this.language;

    @Inject(method = "sendClientSettings", at = @At("TAIL"))
    private void polymer$requestSync(CallbackInfo ci) {
        if (this.client.player != null && (!(this.polymer$oldLang == null || this.polymer$oldLang.equals(this.language)) || InternalClientRegistry.syncRequestsPostGameJoin == 0)) {
            this.polymer$oldLang = this.language;
            PolymerClientProtocol.sendSyncRequest(this.client.player.networkHandler);
        }
    }
}
