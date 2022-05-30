package eu.pb4.polymer.mixin.entity;

import eu.pb4.polymer.api.entity.PolymerEntity;
import eu.pb4.polymer.impl.interfaces.EntityAttachedPacket;
import eu.pb4.polymer.impl.interfaces.PolymerNetworkHandlerExtension;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements PolymerNetworkHandlerExtension {
    @Shadow public abstract ServerPlayerEntity getPlayer();

    @Shadow public ServerPlayerEntity player;

    @ModifyVariable(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"))
    private Packet<?> polymer_skipEffects(Packet<?> packet) {

        if (packet instanceof EntityEquipmentUpdateS2CPacket original && EntityAttachedPacket.get(original) instanceof PolymerEntity polymerEntity) {
            return EntityAttachedPacket.setIfEmpty(
                    new EntityEquipmentUpdateS2CPacket(((Entity) polymerEntity).getId(), polymerEntity.getPolymerVisibleEquipment(original.getEquipmentList(), this.getPlayer())),
                    (Entity) polymerEntity
            );
        }

        return packet;
    }
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    private void polymer_removeInvalid(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {
        if ((packet instanceof EntityEquipmentUpdateS2CPacket original && original.getEquipmentList().isEmpty()) || !EntityAttachedPacket.shouldSend(packet, this.player)) {
            ci.cancel();
        }
    }
}
