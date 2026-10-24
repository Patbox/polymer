package eu.pb4.polymer.autohost.mixin;

import eu.pb4.polymer.autohost.impl.AutoHost;
import eu.pb4.polymer.autohost.impl.AutoHostTask;
import eu.pb4.polymer.resourcepack.impl.PolymerResourcePackMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundClearDialogPacket;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow public abstract void disconnect(Component reason);

    @Shadow
    public abstract void send(Packet<?> packet);

    @Inject(method = "handleCustomClickAction", at = @At("TAIL"))
    private void handlePolymerDisconnect(ServerboundCustomClickActionPacket packet, CallbackInfo ci) {
        if (((Object) this) instanceof ServerConfigurationPacketListenerImpl listener
                && ((ServerConfigurationPacketListenerImplAccessor) listener).polymer$getCurrentTask() instanceof AutoHostTask) {
            if (packet.id().equals(AutoHostTask.DISCONNECT)) {
                this.disconnect(Component.translatable("multiplayer.disconnect.generic"));
            } else if (packet.id().equals(AutoHostTask.JOIN_ON_FAILURE) && AutoHost.config.allowJoiningAfterGenerationFailure && !PolymerResourcePackMod.successState.toBoolean(true)) {
                this.send(ClientboundClearDialogPacket.INSTANCE);
                listener.completeTask(AutoHostTask.KEY);
            }
        }
    }
}
