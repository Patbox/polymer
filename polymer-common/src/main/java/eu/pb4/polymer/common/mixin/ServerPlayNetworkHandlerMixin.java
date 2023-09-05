package eu.pb4.polymer.common.mixin;

import eu.pb4.polymer.common.impl.CommonResourcePackInfoHolder;
import net.minecraft.class_8792;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;setPacketListener(Lnet/minecraft/network/listener/PacketListener;)V"))
    private void polymerCommon$setRP(MinecraftServer server, ClientConnection connection, ServerPlayerEntity player, class_8792 arg, CallbackInfo ci) {
        ((CommonResourcePackInfoHolder) this).polymerCommon$setResourcePackNoEvent(((CommonResourcePackInfoHolder) player).polymerCommon$hasResourcePack());
    }
}
