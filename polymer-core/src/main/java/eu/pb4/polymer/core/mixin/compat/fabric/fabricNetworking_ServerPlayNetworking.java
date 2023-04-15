package eu.pb4.polymer.core.mixin.compat.fabric;

import eu.pb4.polymer.common.impl.CommonImplUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ServerPlayNetworking.class)
public class fabricNetworking_ServerPlayNetworking {
    @Inject(
            method = "send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/fabricmc/fabric/api/networking/v1/FabricPacket;)V",
        at = @At(
                value = "INVOKE",
                target = "Lnet/fabricmc/fabric/api/networking/v1/FabricPacket;write(Lnet/minecraft/network/PacketByteBuf;)V",
                shift = At.Shift.BEFORE
        ), require = 0
    )
    private static void polymerCore$addPlayerContext(ServerPlayerEntity player, @Coerce Object packet, CallbackInfo ci) {
        CommonImplUtils.setPlayer(player);
    }

    @Inject(
            method = "send(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/fabricmc/fabric/api/networking/v1/FabricPacket;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/fabricmc/fabric/api/networking/v1/FabricPacket;write(Lnet/minecraft/network/PacketByteBuf;)V",
                    shift = At.Shift.AFTER
            ), require = 0
    )
    private static void polymerCore$removePlayerContext(ServerPlayerEntity player, @Coerce Object packet, CallbackInfo ci) {
        CommonImplUtils.setPlayer(null);
    }
}
