package eu.pb4.polymer.mixin.other;

import eu.pb4.polymer.api.x.EarlyPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Shadow private PacketListener packetListener;

    @Inject(method = "tick", at = @At("TAIL"))
    private void polymer_tick(CallbackInfo ci) {
        if (this.packetListener instanceof EarlyPlayNetworkHandler e) {
            e.tickInternal();
        }
    }
}
