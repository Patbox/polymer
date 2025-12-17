package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.AutoHostTask;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow public abstract void disconnect(Component reason);

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handlePolymerDisconnect(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        if (packet.id().equals(AutoHostTask.DISCONNECT)) {
            this.disconnect(Component.translatable("multiplayer.disconnect.generic"));
        }
    }
}
