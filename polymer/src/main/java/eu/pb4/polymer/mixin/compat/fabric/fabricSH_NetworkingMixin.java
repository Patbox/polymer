package eu.pb4.polymer.mixin.compat.fabric;

import eu.pb4.polymer.impl.PolymerImplUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.impl.screenhandler.Networking;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Networking.class)
public class fabricSH_NetworkingMixin {
    @Inject(method = "sendOpenPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;<init>(Lio/netty/buffer/ByteBuf;)V"))
    private static void polymer_setPlayer(ServerPlayerEntity player, ExtendedScreenHandlerFactory factory, ScreenHandler handler, int syncId, CallbackInfo ci) {
        PolymerImplUtils.setPlayer(player);
    }

    @Inject(method = "sendOpenPacket", at = @At("TAIL"))
    private static void polymer_removePlayer(ServerPlayerEntity player, ExtendedScreenHandlerFactory factory, ScreenHandler handler, int syncId, CallbackInfo ci) {
        PolymerImplUtils.setPlayer(player);
    }
}
