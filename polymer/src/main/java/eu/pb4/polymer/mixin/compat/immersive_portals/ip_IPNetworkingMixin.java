package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.PolymerImplUtils;
import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(IPNetworking.class)
public class ip_IPNetworkingMixin {
    @Inject(method = "sendRedirectedMessage", at = @At("HEAD"))
    private static void polymer_setPlayerNow(ServerPlayerEntity player, RegistryKey<World> dimension, Packet packet, CallbackInfo ci) {
        PolymerImplUtils.setPlayer(player);
    }

    @ModifyArg(method = "sendRedirectedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
    private static Packet<?> polymer_resetPlayerNow(Packet<?> packet) {
        PolymerImplUtils.setPlayer(null);
        return ((IPAttachedPacket) packet).polymer_ip_setSkip(true);
    }

    @Inject(method = "createRedirectedMessage", at = @At("RETURN"))
    private static void polymer_catchPacket(RegistryKey<World> dimension, Packet packet, CallbackInfoReturnable<Packet> cir) {
        ((IPAttachedPacket) cir.getReturnValue()).polymer_ip_setAttachedPacket(packet, dimension);
    }
}
