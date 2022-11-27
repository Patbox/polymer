package eu.pb4.polymer.core.mixin.block.packet;

import eu.pb4.polymer.core.impl.networking.BlockPacketUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", shift = At.Shift.AFTER))
    private void polymer$catchBlockUpdates(Packet<?> packet, PacketCallbacks callbacks, CallbackInfo ci) {
        try {
            BlockPacketUtil.sendFromPacket(packet, (ServerPlayNetworkHandler) (Object) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
