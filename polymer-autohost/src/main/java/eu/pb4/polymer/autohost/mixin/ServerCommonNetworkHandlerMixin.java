package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostTask;
import net.minecraft.network.packet.c2s.common.CustomClickActionC2SPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerCommonNetworkHandlerMixin {
    @Shadow public abstract void disconnect(Text reason);

    @Inject(method = "onCustomClickAction", at = @At("TAIL"))
    private void handlePolymerDisconnect(CustomClickActionC2SPacket packet, CallbackInfo ci) {
        if (packet.id().equals(AutoHostTask.DISCONNECT)) {
            this.disconnect(Text.translatable("multiplayer.disconnect.generic"));
        }
    }
}
