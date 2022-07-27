package eu.pb4.polymer.mixin.block.packet;

import eu.pb4.polymer.impl.networking.BlockPacketUtil;
import net.minecraft.class_7648;
import net.minecraft.network.Packet;
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

    @Inject(method = "method_14369", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;method_10752(Lnet/minecraft/network/Packet;Lnet/minecraft/class_7648;)V", shift = At.Shift.AFTER))
    private void polymer_catchBlockUpdates(Packet<?> packet, class_7648 arg, CallbackInfo ci) {
        try {
            BlockPacketUtil.sendFromPacket(packet, (ServerPlayNetworkHandler) (Object) this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
