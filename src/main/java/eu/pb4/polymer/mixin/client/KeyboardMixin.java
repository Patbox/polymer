package eu.pb4.polymer.mixin.client;

import eu.pb4.polymer.impl.client.networking.PolymerClientProtocol;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "processF3", at = @At("TAIL"))
    private void polymer_catchChange(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == 72) {
            PolymerClientProtocol.sendTooltipContext(this.client.getNetworkHandler());
        }
    }
}
