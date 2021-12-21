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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(IPNetworking.class)
public class ip_IPNetworkingMixin {
    @Inject(method = "sendRedirectedMessage", at = @At("HEAD"))
    private static void polymer_setPlayerNow(ServerPlayerEntity player, RegistryKey<World> dimension, Packet packet, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(player);
    }

    @Inject(method = "sendRedirectedMessage", at = @At("TAIL"))
    private static void polymer_resetPlayerNow(ServerPlayerEntity player, RegistryKey<World> dimension, Packet packet, CallbackInfo ci) {
        PolymerImplUtils.playerTargetHack.set(null);
    }

    @Inject(method = "createRedirectedMessage", at = @At("RETURN"))
    private static void polymer_catchPacket(RegistryKey<World> dimension, Packet packet, CallbackInfoReturnable<Packet> cir) {
        ((IPAttachedPacket) cir.getReturnValue()).polymer_ip_setAttachedPacket(packet, dimension);
    }
}
