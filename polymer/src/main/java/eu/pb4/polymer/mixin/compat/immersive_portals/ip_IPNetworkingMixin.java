package eu.pb4.polymer.mixin.compat.immersive_portals;

import eu.pb4.polymer.impl.compat.IPAttachedPacket;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.platform_specific.IPNetworking;

@Mixin(IPNetworking.class)
public class ip_IPNetworkingMixin {
    @Redirect(method = "createRedirectedMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;write(Lnet/minecraft/network/PacketByteBuf;)V"))
    private static void polymer_dontWrite(Packet instance, PacketByteBuf buf) {}

    @Inject(method = "createRedirectedMessage", at = @At("RETURN"))
    private static void  polymer_attachPacket(RegistryKey<World> dimension, Packet packet, CallbackInfoReturnable<Packet> cir) {
        ((IPAttachedPacket) cir.getReturnValue()).polymer_ip_setAttachedPacket(packet, dimension);
    }
}
